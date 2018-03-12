/*
 * $Id: JSONParser.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-4-15
 */
package net.robotictip.parser.simple.parser;

import net.robotictip.parser.simple.JACKArray;
import net.robotictip.parser.simple.JACKObject;

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
public class JACKReader {
	public static final int S_INIT=0;
	public static final int S_IN_FINISHED_VALUE=1;//string,number,boolean,null,object,array
	public static final int S_IN_OBJECT=2;
	public static final int S_IN_ARRAY=3;
	public static final int S_PASSED_PAIR_KEY=4;
	public static final int S_IN_PAIR_VALUE=5;
	public static final int S_END=6;
	public static final int S_IN_ERROR=-1;
	
	private LinkedList managerStatusStack;
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
        managerStatusStack = null;
    }
    
    /**
     * Reset the parser to the initial state with a new character reader.
     * 
     * @param in - The new character reader.
     * @throws IOException
     * @throws ParseTrouble
     */
	public void reset(Reader in){
		lexer.yyreset(in);
		reset();
	}
	
	/**
	 * @return The position of the beginning of the current token.
	 */
	public int getPosition(){
		return lexer.grabPosition();
	}
	
	public Object parse(String s) throws ParseTrouble {
		return parse(s, (ContainerFactory)null);
	}
	
	public Object parse(String s, ContainerFactory containerFactory) throws ParseTrouble {
		StringReader in=new StringReader(s);
		try{
			return parse(in, containerFactory);
		}
		catch(IOException ie){
			/*
			 * Actually it will never happen.
			 */
			throw new ParseTrouble(-1, ParseTrouble.ERROR_UNEXPECTED_EXCEPTION, ie);
		}
	}
	
	public Object parse(Reader in) throws IOException, ParseTrouble {
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
	 * @throws ParseTrouble
	 */
	public Object parse(Reader in, ContainerFactory containerFactory) throws IOException, ParseTrouble {
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
						valueStack.addFirst(generateObjectContainer(containerFactory));
						break;
					case Yytoken.TYPE_LEFT_SQUARE:
						status=S_IN_ARRAY;
						statusStack.addFirst(new Integer(status));
						valueStack.addFirst(generateArrayContainer(containerFactory));
						break;
					default:
						status=S_IN_ERROR;
					}//inner switch
					break;
					
				case S_IN_FINISHED_VALUE:
					if(token.type==Yytoken.TYPE_EOF)
						return valueStack.removeFirst();
					else
						throw new ParseTrouble(getPosition(), ParseTrouble.ERROR_UNEXPECTED_TOKEN, token);
					
				case S_IN_OBJECT:
					switch(token.type){
					case Yytoken.TYPE_COMMA:
						break;
					case Yytoken.TYPE_VALUE:
						if(token.value instanceof String){
                            parseGateKeeper(statusStack, valueStack);
						}
						else{
							status=S_IN_ERROR;
						}
						break;
					case Yytoken.TYPE_RIGHT_BRACE:
						if(valueStack.size()>1){
                            parseHelper(statusStack, valueStack);
						}
						else{
                            parseCoordinator();
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
						List newArray= generateArrayContainer(containerFactory);
						parent.put(key,newArray);
						status=S_IN_ARRAY;
						statusStack.addFirst(new Integer(status));
						valueStack.addFirst(newArray);
						break;
					case Yytoken.TYPE_LEFT_BRACE:
						statusStack.removeFirst();
						key=(String)valueStack.removeFirst();
						parent=(Map)valueStack.getFirst();
						Map newObject= generateObjectContainer(containerFactory);
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
							statusStack.removeFirst();
							valueStack.removeFirst();
							status=peekStatus(statusStack);
						}
						else{
                            parseFunction();
                        }
						break;
					case Yytoken.TYPE_LEFT_BRACE:
						val=(List)valueStack.getFirst();
						Map newObject= generateObjectContainer(containerFactory);
						val.add(newObject);
						status=S_IN_OBJECT;
						statusStack.addFirst(new Integer(status));
						valueStack.addFirst(newObject);
						break;
					case Yytoken.TYPE_LEFT_SQUARE:
						val=(List)valueStack.getFirst();
						List newArray= generateArrayContainer(containerFactory);
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
					throw new ParseTrouble(getPosition(), ParseTrouble.ERROR_UNEXPECTED_TOKEN, token);
				}//switch
				if(status==S_IN_ERROR){
					throw new ParseTrouble(getPosition(), ParseTrouble.ERROR_UNEXPECTED_TOKEN, token);
				}
			}while(token.type!=Yytoken.TYPE_EOF);
		}
		catch(IOException ie){
			throw ie;
		}
		
		throw new ParseTrouble(getPosition(), ParseTrouble.ERROR_UNEXPECTED_TOKEN, token);
	}

    private void parseFunction() {
        status=S_IN_FINISHED_VALUE;
    }

    private void parseCoordinator() {
        status=S_IN_FINISHED_VALUE;
    }

    private void parseHelper(LinkedList statusStack, LinkedList valueStack) {
        statusStack.removeFirst();
        valueStack.removeFirst();
        status=peekStatus(statusStack);
    }

    private void parseGateKeeper(LinkedList statusStack, LinkedList valueStack) {
        String key=(String)token.value;
        valueStack.addFirst(key);
        status=S_PASSED_PAIR_KEY;
        statusStack.addFirst(new Integer(status));
    }

    private void nextToken() throws ParseTrouble, IOException{
		token = lexer.yylex();
		if(token == null)
			token = new YytokenBuilder().assignType(Yytoken.TYPE_EOF).setValue(null).generateYytoken();
	}
	
	private Map generateObjectContainer(ContainerFactory containerFactory){
		if(containerFactory == null)
			return new JACKObject();
		Map m = containerFactory.generateObjectContainer();
		
		if(m == null)
			return new JACKObject();
		return m;
	}
	
	private List generateArrayContainer(ContainerFactory containerFactory){
		if(containerFactory == null)
			return new JACKArray();
		List l = containerFactory.creatArrayContainer();
		
		if(l == null)
			return new JACKArray();
		return l;
	}
	
	public void parse(String s, ContentManager contentManager) throws ParseTrouble {
		parse(s, contentManager, false);
	}
	
	public void parse(String s, ContentManager contentManager, boolean isResume) throws ParseTrouble {
		StringReader in=new StringReader(s);
		try{
			parse(in, contentManager, isResume);
		}
		catch(IOException ie){
			/*
			 * Actually it will never happen.
			 */
			throw new ParseTrouble(-1, ParseTrouble.ERROR_UNEXPECTED_EXCEPTION, ie);
		}
	}
	
	public void parse(Reader in, ContentManager contentManager) throws IOException, ParseTrouble {
		parse(in, contentManager, false);
	}
	
	/**
	 * Stream processing of JSON text.
	 * 
	 * @see ContentManager
	 * 
	 * @param in
	 * @param contentManager
	 * @param isResume - Indicates if it continues previous parsing operation.
     *                   If set to true, resume parsing the old stream, and parameter 'in' will be ignored. 
	 *                   If this method is called for the first time in this instance, isResume will be ignored.
	 * 
	 * @throws IOException
	 * @throws ParseTrouble
	 */
	public void parse(Reader in, ContentManager contentManager, boolean isResume) throws IOException, ParseTrouble {
		if(!isResume){
            parseGateKeeper(in);
		}
		else{
			if(managerStatusStack == null){
				isResume = false;
				reset(in);
				managerStatusStack = new LinkedList();
			}
		}
		
		LinkedList statusStack = managerStatusStack;
		
		try{
			do{
				switch(status){
				case S_INIT:
					contentManager.startJACK();
					nextToken();
					switch(token.type){
					case Yytoken.TYPE_VALUE:
						status=S_IN_FINISHED_VALUE;
						statusStack.addFirst(new Integer(status));
						if(!contentManager.primitive(token.value))
							return;
						break;
					case Yytoken.TYPE_LEFT_BRACE:
						status=S_IN_OBJECT;
						statusStack.addFirst(new Integer(status));
						if(!contentManager.startObject())
							return;
						break;
					case Yytoken.TYPE_LEFT_SQUARE:
						status=S_IN_ARRAY;
						statusStack.addFirst(new Integer(status));
						if(!contentManager.startArray())
							return;
						break;
					default:
						status=S_IN_ERROR;
					}//inner switch
					break;
					
				case S_IN_FINISHED_VALUE:
					nextToken();
					if(token.type==Yytoken.TYPE_EOF){
						contentManager.endJACK();
						status = S_END;
						return;
					}
					else{
                        parseSupervisor();
					}
			
				case S_IN_OBJECT:
					nextToken();
					switch(token.type){
					case Yytoken.TYPE_COMMA:
						break;
					case Yytoken.TYPE_VALUE:
						if(token.value instanceof String){
							String key=(String)token.value;
							status=S_PASSED_PAIR_KEY;
							statusStack.addFirst(new Integer(status));
							if(!contentManager.startObjectEntry(key))
								return;
						}
						else{
                            parseTarget();
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
						if(!contentManager.endObject())
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
						if(!contentManager.primitive(token.value))
							return;
						if(!contentManager.endObjectEntry())
							return;
						break;
					case Yytoken.TYPE_LEFT_SQUARE:
						statusStack.removeFirst();
						statusStack.addFirst(new Integer(S_IN_PAIR_VALUE));
						status=S_IN_ARRAY;
						statusStack.addFirst(new Integer(status));
						if(!contentManager.startArray())
							return;
						break;
					case Yytoken.TYPE_LEFT_BRACE:
						statusStack.removeFirst();
						statusStack.addFirst(new Integer(S_IN_PAIR_VALUE));
						status=S_IN_OBJECT;
						statusStack.addFirst(new Integer(status));
						if(!contentManager.startObject())
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
					if(!contentManager.endObjectEntry())
						return;
					break;
					
				case S_IN_ARRAY:
					nextToken();
					switch(token.type){
					case Yytoken.TYPE_COMMA:
						break;
					case Yytoken.TYPE_VALUE:
						if(!contentManager.primitive(token.value))
							return;
						break;
					case Yytoken.TYPE_RIGHT_SQUARE:
						if(statusStack.size()>1){
							statusStack.removeFirst();
							status=peekStatus(statusStack);
						}
						else{
							status=S_IN_FINISHED_VALUE;
						}
						if(!contentManager.endArray())
							return;
						break;
					case Yytoken.TYPE_LEFT_BRACE:
						status=S_IN_OBJECT;
						statusStack.addFirst(new Integer(status));
						if(!contentManager.startObject())
							return;
						break;
					case Yytoken.TYPE_LEFT_SQUARE:
						status=S_IN_ARRAY;
						statusStack.addFirst(new Integer(status));
						if(!contentManager.startArray())
							return;
						break;
					default:
						status=S_IN_ERROR;
					}//inner switch
					break;
					
				case S_END:
					return;
					
				case S_IN_ERROR:
					throw new ParseTrouble(getPosition(), ParseTrouble.ERROR_UNEXPECTED_TOKEN, token);
				}//switch
				if(status==S_IN_ERROR){
                    new JACKReaderTarget().invoke();
                }
			}while(token.type!=Yytoken.TYPE_EOF);
		}
		catch(IOException ie){
			status = S_IN_ERROR;
			throw ie;
		}
		catch(ParseTrouble pe){
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
		throw new ParseTrouble(getPosition(), ParseTrouble.ERROR_UNEXPECTED_TOKEN, token);
	}

    private void parseHerder() {
        status=S_IN_FINISHED_VALUE;
    }

    private void parseTarget() {
        status=S_IN_ERROR;
    }

    private void parseSupervisor() throws ParseTrouble {
        status = S_IN_ERROR;
        throw new ParseTrouble(getPosition(), ParseTrouble.ERROR_UNEXPECTED_TOKEN, token);
    }

    private void parseGateKeeper(Reader in) {
        reset(in);
        managerStatusStack = new LinkedList();
    }

    private class JACKReaderTarget {
        public void invoke() throws ParseTrouble {
            throw new ParseTrouble(getPosition(), ParseTrouble.ERROR_UNEXPECTED_TOKEN, token);
        }
    }
}
