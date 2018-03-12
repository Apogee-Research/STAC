/*
 * $Id: JSONParser.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-4-15
 */
package net.roboticapex.parser.simple.grabber;

import net.roboticapex.parser.simple.PARSINGArray;
import net.roboticapex.parser.simple.PARSINGObject;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Parser for JSON text. Please note that JSONParser is NOT thread-safe.
 * 
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public class PARSINGParser {
	public static final int S_INIT=0;
	public static final int S_IN_FINISHED_VALUE=1;//string,number,boolean,null,object,array
	public static final int S_IN_OBJECT=2;
	public static final int S_IN_ARRAY=3;
	public static final int S_PASSED_PAIR_KEY=4;
	public static final int S_IN_PAIR_VALUE=5;
	public static final int S_END=6;
	public static final int S_IN_ERROR=-1;
	
	private LinkedList handlerStatusStack;
	private Yylex lexer = new Yylex((Reader)null);
	private Yytoken token = null;
	private int status = S_INIT;
	
	private int peekStatus(LinkedList statusStack){
		if(statusStack.size()==0)
			return -1;
		Integer status=(Integer)statusStack.getFirst();
		return status.intValue();
	}
	
    /**
     *  Reset the parser to the initial state without resetting the underlying reader.
     *
     */
    public void reset(){
        token = null;
        status = S_INIT;
        handlerStatusStack = null;
    }
    
    /**
     * Reset the parser to the initial state with a new character reader.
     * 
     * @param in - The new character reader.
     * @throws IOException
     * @throws ParseDeviation
     */
	public void reset(Reader in){
		lexer.yyreset(in);
		reset();
	}
	
	/**
	 * @return The position of the beginning of the current token.
	 */
	public int pullPosition(){
		return lexer.pullPosition();
	}
	
	public Object parse(String s) throws ParseDeviation {
		return parse(s, (ContainerFactory)null);
	}
	
	public Object parse(String s, ContainerFactory containerFactory) throws ParseDeviation {
		StringReader in=new StringReader(s);
		try{
			return parse(in, containerFactory);
		}
		catch(IOException ie){
			/*
			 * Actually it will never happen.
			 */
			throw new ParseDeviationBuilder().fixPosition(-1).setErrorType(ParseDeviation.ERROR_UNEXPECTED_EXCEPTION).setUnexpectedObject(ie).makeParseDeviation();
		}
	}
	
	public Object parse(Reader in) throws IOException, ParseDeviation {
		return parse(in, (ContainerFactory)null);
	}
	
	/**
	 * Parse JSON text into java object from the input source.
	 * 	
	 * @param in
     * @param containerFactory - Use this factory to createyour own JSON object and JSON array containers.
	 * @return Instance of the following:
	 *  org.json.simple.JSONObject,
	 * 	org.json.simple.JSONArray,
	 * 	java.lang.String,
	 * 	java.lang.Number,
	 * 	java.lang.Boolean,
	 * 	null
	 * 
	 * @throws IOException
	 * @throws ParseDeviation
	 */
	public Object parse(Reader in, ContainerFactory containerFactory) throws IOException, ParseDeviation {
		reset(in);
		LinkedList statusStack = new LinkedList();
		LinkedList valueStack = new LinkedList();
		
		try{
			do{
				nextToken();
				switch(status){
				case S_INIT:
					switch(token.type){
					case Yytoken.TYPE_VALUE:
						status=S_IN_FINISHED_VALUE;
						statusStack.addFirst(new Integer(status));
						valueStack.addFirst(token.value);
						break;
					case Yytoken.TYPE_LEFT_BRACE:
						status=S_IN_OBJECT;
						statusStack.addFirst(new Integer(status));
						valueStack.addFirst(makeObjectContainer(containerFactory));
						break;
					case Yytoken.TYPE_LEFT_SQUARE:
						status=S_IN_ARRAY;
						statusStack.addFirst(new Integer(status));
						valueStack.addFirst(makeArrayContainer(containerFactory));
						break;
					default:
						status=S_IN_ERROR;
					}//inner switch
					break;
					
				case S_IN_FINISHED_VALUE:
					if(token.type==Yytoken.TYPE_EOF)
						return valueStack.removeFirst();
					else
						throw new ParseDeviationBuilder().fixPosition(pullPosition()).setErrorType(ParseDeviation.ERROR_UNEXPECTED_TOKEN).setUnexpectedObject(token).makeParseDeviation();
					
				case S_IN_OBJECT:
					switch(token.type){
					case Yytoken.TYPE_COMMA:
						break;
					case Yytoken.TYPE_VALUE:
						if(token.value instanceof String){
                            parseCoordinator(statusStack, valueStack);
						}
						else{
							status=S_IN_ERROR;
						}
						break;
					case Yytoken.TYPE_RIGHT_BRACE:
						if(valueStack.size()>1){
							statusStack.removeFirst();
							valueStack.removeFirst();
							status=peekStatus(statusStack);
						}
						else{
                            parseEngine();
                        }
						break;
					default:
						status=S_IN_ERROR;
						break;
					}//inner switch
					break;
					
				case S_PASSED_PAIR_KEY:
					switch(token.type){
					case Yytoken.TYPE_COLON:
						break;
					case Yytoken.TYPE_VALUE:
						statusStack.removeFirst();
						String key=(String)valueStack.removeFirst();
						Map parent=(Map)valueStack.getFirst();
						parent.put(key,token.value);
						status=peekStatus(statusStack);
						break;
					case Yytoken.TYPE_LEFT_SQUARE:
						statusStack.removeFirst();
						key=(String)valueStack.removeFirst();
						parent=(Map)valueStack.getFirst();
						List newArray= makeArrayContainer(containerFactory);
						parent.put(key,newArray);
						status=S_IN_ARRAY;
						statusStack.addFirst(new Integer(status));
						valueStack.addFirst(newArray);
						break;
					case Yytoken.TYPE_LEFT_BRACE:
						statusStack.removeFirst();
						key=(String)valueStack.removeFirst();
						parent=(Map)valueStack.getFirst();
						Map newObject= makeObjectContainer(containerFactory);
						parent.put(key,newObject);
						status=S_IN_OBJECT;
						statusStack.addFirst(new Integer(status));
						valueStack.addFirst(newObject);
						break;
					default:
						status=S_IN_ERROR;
					}
					break;
					
				case S_IN_ARRAY:
					switch(token.type){
					case Yytoken.TYPE_COMMA:
						break;
					case Yytoken.TYPE_VALUE:
						List val=(List)valueStack.getFirst();
						val.add(token.value);
						break;
					case Yytoken.TYPE_RIGHT_SQUARE:
						if(valueStack.size()>1){
                            parseGateKeeper(statusStack, valueStack);
						}
						else{
							status=S_IN_FINISHED_VALUE;
						}
						break;
					case Yytoken.TYPE_LEFT_BRACE:
						val=(List)valueStack.getFirst();
						Map newObject= makeObjectContainer(containerFactory);
						val.add(newObject);
						status=S_IN_OBJECT;
						statusStack.addFirst(new Integer(status));
						valueStack.addFirst(newObject);
						break;
					case Yytoken.TYPE_LEFT_SQUARE:
						val=(List)valueStack.getFirst();
						List newArray= makeArrayContainer(containerFactory);
						val.add(newArray);
						status=S_IN_ARRAY;
						statusStack.addFirst(new Integer(status));
						valueStack.addFirst(newArray);
						break;
					default:
						status=S_IN_ERROR;
					}//inner switch
					break;
				case S_IN_ERROR:
					throw new ParseDeviationBuilder().fixPosition(pullPosition()).setErrorType(ParseDeviation.ERROR_UNEXPECTED_TOKEN).setUnexpectedObject(token).makeParseDeviation();
				}//switch
				if(status==S_IN_ERROR){
					throw new ParseDeviationBuilder().fixPosition(pullPosition()).setErrorType(ParseDeviation.ERROR_UNEXPECTED_TOKEN).setUnexpectedObject(token).makeParseDeviation();
				}
			}while(token.type!=Yytoken.TYPE_EOF);
		}
		catch(IOException ie){
			throw ie;
		}
		
		throw new ParseDeviationBuilder().fixPosition(pullPosition()).setErrorType(ParseDeviation.ERROR_UNEXPECTED_TOKEN).setUnexpectedObject(token).makeParseDeviation();
	}

    private void parseGateKeeper(LinkedList statusStack, LinkedList valueStack) {
        statusStack.removeFirst();
        valueStack.removeFirst();
        status=peekStatus(statusStack);
    }

    private void parseEngine() {
        status=S_IN_FINISHED_VALUE;
    }

    private void parseCoordinator(LinkedList statusStack, LinkedList valueStack) {
        String key=(String)token.value;
        valueStack.addFirst(key);
        status=S_PASSED_PAIR_KEY;
        statusStack.addFirst(new Integer(status));
    }

    private void nextToken() throws ParseDeviation, IOException{
		token = lexer.yylex();
		if(token == null)
			token = new Yytoken(Yytoken.TYPE_EOF, null);
	}
	
	private Map makeObjectContainer(ContainerFactory containerFactory){
		if(containerFactory == null)
			return new PARSINGObject();
		Map m = containerFactory.makeObjectContainer();
		
		if(m == null)
			return new PARSINGObject();
		return m;
	}
	
	private List makeArrayContainer(ContainerFactory containerFactory){
		if(containerFactory == null)
			return new PARSINGArray();
		List l = containerFactory.creatArrayContainer();
		
		if(l == null)
			return new PARSINGArray();
		return l;
	}
	
	public void parse(String s, ContentHandler contentHandler) throws ParseDeviation {
		parse(s, contentHandler, false);
	}
	
	public void parse(String s, ContentHandler contentHandler, boolean isResume) throws ParseDeviation {
		StringReader in=new StringReader(s);
		try{
			parse(in, contentHandler, isResume);
		}
		catch(IOException ie){
			/*
			 * Actually it will never happen.
			 */
			throw new ParseDeviationBuilder().fixPosition(-1).setErrorType(ParseDeviation.ERROR_UNEXPECTED_EXCEPTION).setUnexpectedObject(ie).makeParseDeviation();
		}
	}
	
	public void parse(Reader in, ContentHandler contentHandler) throws IOException, ParseDeviation {
		parse(in, contentHandler, false);
	}
	
	/**
	 * Stream processing of JSON text.
	 * 
	 * @see ContentHandler
	 * 
	 * @param in
	 * @param contentHandler
	 * @param isResume - Indicates if it continues previous parsing operation.
     *                   If set to true, resume parsing the old stream, and parameter 'in' will be ignored. 
	 *                   If this method is called for the first time in this instance, isResume will be ignored.
	 * 
	 * @throws IOException
	 * @throws ParseDeviation
	 */
	public void parse(Reader in, ContentHandler contentHandler, boolean isResume) throws IOException, ParseDeviation {
		if(!isResume){
            parseService(in);
		}
		else{
			if(handlerStatusStack == null){
				isResume = false;
				reset(in);
				handlerStatusStack = new LinkedList();
			}
		}
		
		LinkedList statusStack = handlerStatusStack;	
		
		try{
			do{
				switch(status){
				case S_INIT:
					contentHandler.startPARSING();
					nextToken();
					switch(token.type){
					case Yytoken.TYPE_VALUE:
						status=S_IN_FINISHED_VALUE;
						statusStack.addFirst(new Integer(status));
						if(!contentHandler.primitive(token.value))
							return;
						break;
					case Yytoken.TYPE_LEFT_BRACE:
						status=S_IN_OBJECT;
						statusStack.addFirst(new Integer(status));
						if(!contentHandler.startObject())
							return;
						break;
					case Yytoken.TYPE_LEFT_SQUARE:
						status=S_IN_ARRAY;
						statusStack.addFirst(new Integer(status));
						if(!contentHandler.startArray())
							return;
						break;
					default:
						status=S_IN_ERROR;
					}//inner switch
					break;
					
				case S_IN_FINISHED_VALUE:
					nextToken();
					if(token.type==Yytoken.TYPE_EOF){
                        parseFunction(contentHandler);
                        return;
					}
					else{
                        new PARSINGParserEntity().invoke();
					}
			
				case S_IN_OBJECT:
					nextToken();
					switch(token.type){
					case Yytoken.TYPE_COMMA:
						break;
					case Yytoken.TYPE_VALUE:
						if(token.value instanceof String){
                            if (parseAssist(contentHandler, statusStack)) return;
                        }
						else{
							status=S_IN_ERROR;
						}
						break;
					case Yytoken.TYPE_RIGHT_BRACE:
						if(statusStack.size()>1){
							statusStack.removeFirst();
							status=peekStatus(statusStack);
						}
						else{
                            parseHerder();
                        }
						if(!contentHandler.endObject())
							return;
						break;
					default:
						status=S_IN_ERROR;
						break;
					}//inner switch
					break;
					
				case S_PASSED_PAIR_KEY:
					nextToken();
					switch(token.type){
					case Yytoken.TYPE_COLON:
						break;
					case Yytoken.TYPE_VALUE:
						statusStack.removeFirst();
						status=peekStatus(statusStack);
						if(!contentHandler.primitive(token.value))
							return;
						if(!contentHandler.endObjectEntry())
							return;
						break;
					case Yytoken.TYPE_LEFT_SQUARE:
						statusStack.removeFirst();
						statusStack.addFirst(new Integer(S_IN_PAIR_VALUE));
						status=S_IN_ARRAY;
						statusStack.addFirst(new Integer(status));
						if(!contentHandler.startArray())
							return;
						break;
					case Yytoken.TYPE_LEFT_BRACE:
						statusStack.removeFirst();
						statusStack.addFirst(new Integer(S_IN_PAIR_VALUE));
						status=S_IN_OBJECT;
						statusStack.addFirst(new Integer(status));
						if(!contentHandler.startObject())
							return;
						break;
					default:
						status=S_IN_ERROR;
					}
					break;
				
				case S_IN_PAIR_VALUE:
					/*
					 * S_IN_PAIR_VALUE is just a marker to indicate the end of an object entry, it doesn't proccess any token,
					 * therefore delay consuming token until next round.
					 */
					statusStack.removeFirst();
					status = peekStatus(statusStack);
					if(!contentHandler.endObjectEntry())
						return;
					break;
					
				case S_IN_ARRAY:
					nextToken();
					switch(token.type){
					case Yytoken.TYPE_COMMA:
						break;
					case Yytoken.TYPE_VALUE:
						if(!contentHandler.primitive(token.value))
							return;
						break;
					case Yytoken.TYPE_RIGHT_SQUARE:
						if(statusStack.size()>1){
                            parseSupervisor(statusStack);
						}
						else{
                            parseCoordinator();
                        }
						if(!contentHandler.endArray())
							return;
						break;
					case Yytoken.TYPE_LEFT_BRACE:
						status=S_IN_OBJECT;
						statusStack.addFirst(new Integer(status));
						if(!contentHandler.startObject())
							return;
						break;
					case Yytoken.TYPE_LEFT_SQUARE:
						status=S_IN_ARRAY;
						statusStack.addFirst(new Integer(status));
						if(!contentHandler.startArray())
							return;
						break;
					default:
						status=S_IN_ERROR;
					}//inner switch
					break;
					
				case S_END:
					return;
					
				case S_IN_ERROR:
					throw new ParseDeviationBuilder().fixPosition(pullPosition()).setErrorType(ParseDeviation.ERROR_UNEXPECTED_TOKEN).setUnexpectedObject(token).makeParseDeviation();
				}//switch
				if(status==S_IN_ERROR){
                    parseHelp();
                }
			}while(token.type!=Yytoken.TYPE_EOF);
		}
		catch(IOException ie){
			status = S_IN_ERROR;
			throw ie;
		}
		catch(ParseDeviation pe){
			status = S_IN_ERROR;
			throw pe;
		}
		catch(RuntimeException re){
			status = S_IN_ERROR;
			throw re;
		}
		catch(Error e){
			status = S_IN_ERROR;
			throw e;
		}
		
		status = S_IN_ERROR;
		throw new ParseDeviationBuilder().fixPosition(pullPosition()).setErrorType(ParseDeviation.ERROR_UNEXPECTED_TOKEN).setUnexpectedObject(token).makeParseDeviation();
	}

    private void parseHelp() throws ParseDeviation {
        throw new ParseDeviationBuilder().fixPosition(pullPosition()).setErrorType(ParseDeviation.ERROR_UNEXPECTED_TOKEN).setUnexpectedObject(token).makeParseDeviation();
    }

    private void parseCoordinator() {
        status=S_IN_FINISHED_VALUE;
    }

    private void parseSupervisor(LinkedList statusStack) {
        statusStack.removeFirst();
        status=peekStatus(statusStack);
    }

    private void parseHerder() {
        status=S_IN_FINISHED_VALUE;
    }

    private boolean parseAssist(ContentHandler contentHandler, LinkedList statusStack) throws ParseDeviation, IOException {
        if (new PARSINGParserHandler(contentHandler, statusStack).invoke()) return true;
        return false;
    }

    private void parseFunction(ContentHandler contentHandler) throws ParseDeviation, IOException {
        contentHandler.endPARSING();
        status = S_END;
        return;
    }

    private void parseService(Reader in) {
        reset(in);
        handlerStatusStack = new LinkedList();
    }

    private class PARSINGParserEntity {
        public void invoke() throws ParseDeviation {
            status = S_IN_ERROR;
            throw new ParseDeviationBuilder().fixPosition(pullPosition()).setErrorType(ParseDeviation.ERROR_UNEXPECTED_TOKEN).setUnexpectedObject(token).makeParseDeviation();
        }
    }

    private class PARSINGParserHandler {
        private boolean myResult;
        private ContentHandler contentHandler;
        private LinkedList statusStack;

        public PARSINGParserHandler(ContentHandler contentHandler, LinkedList statusStack) {
            this.contentHandler = contentHandler;
            this.statusStack = statusStack;
        }

        boolean is() {
            return myResult;
        }

        public boolean invoke() throws ParseDeviation, IOException {
            String key=(String)token.value;
            status=S_PASSED_PAIR_KEY;
            statusStack.addFirst(new Integer(status));
            if(!contentHandler.startObjectEntry(key))
                return true;
            return false;
        }
    }
}
