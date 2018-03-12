/*
 * $Id: JSONParser.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-4-15
 */
package com.networkapex.parsing.simple.parser;

import com.networkapex.parsing.simple.PARSERArray;
import com.networkapex.parsing.simple.PARSERObject;

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
public class PARSERReader {
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
     * @throws ParseRaiser
     */
	public void reset(Reader in){
		lexer.yyreset(in);
		reset();
	}
	
	/**
	 * @return The position of the beginning of the current token.
	 */
	public int fetchPosition(){
		return lexer.pullPosition();
	}
	
	public Object parse(String s) throws ParseRaiser {
		return parse(s, (ContainerFactory)null);
	}
	
	public Object parse(String s, ContainerFactory containerFactory) throws ParseRaiser {
		StringReader in=new StringReader(s);
		try{
			return parse(in, containerFactory);
		}
		catch(IOException ie){
			/*
			 * Actually it will never happen.
			 */
			throw new ParseRaiser(-1, ParseRaiser.ERROR_UNEXPECTED_EXCEPTION, ie);
		}
	}
	
	public Object parse(Reader in) throws IOException, ParseRaiser {
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
	 * @throws ParseRaiser
	 */
	public Object parse(Reader in, ContainerFactory containerFactory) throws IOException, ParseRaiser {
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
						throw new ParseRaiser(fetchPosition(), ParseRaiser.ERROR_UNEXPECTED_TOKEN, token);
					
				case S_IN_OBJECT:
					switch(token.type){
					case Yytoken.TYPE_COMMA:
						break;
					case Yytoken.TYPE_VALUE:
						if(token.value instanceof String){
                            new PARSERReaderCoordinator(statusStack, valueStack).invoke();
						}
						else{
							status=S_IN_ERROR;
						}
						break;
					case Yytoken.TYPE_RIGHT_BRACE:
						if(valueStack.size()>1){
                            parseTarget(statusStack, valueStack);
						}
						else{
                            parseExecutor();
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
                            parseService(statusStack, valueStack);
						}
						else{
							status=S_IN_FINISHED_VALUE;
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
					throw new ParseRaiser(fetchPosition(), ParseRaiser.ERROR_UNEXPECTED_TOKEN, token);
				}//switch
				if(status==S_IN_ERROR){
					throw new ParseRaiser(fetchPosition(), ParseRaiser.ERROR_UNEXPECTED_TOKEN, token);
				}
			}while(token.type!=Yytoken.TYPE_EOF);
		}
		catch(IOException ie){
			throw ie;
		}
		
		throw new ParseRaiser(fetchPosition(), ParseRaiser.ERROR_UNEXPECTED_TOKEN, token);
	}

    private void parseService(LinkedList statusStack, LinkedList valueStack) {
        statusStack.removeFirst();
        valueStack.removeFirst();
        status=peekStatus(statusStack);
    }

    private void parseExecutor() {
        status=S_IN_FINISHED_VALUE;
    }

    private void parseTarget(LinkedList statusStack, LinkedList valueStack) {
        statusStack.removeFirst();
        valueStack.removeFirst();
        status=peekStatus(statusStack);
    }

    private void nextToken() throws ParseRaiser, IOException{
		token = lexer.yylex();
		if(token == null)
			token = new YytokenBuilder().defineType(Yytoken.TYPE_EOF).setValue(null).generateYytoken();
	}
	
	private Map generateObjectContainer(ContainerFactory containerFactory){
		if(containerFactory == null)
			return new PARSERObject();
		Map m = containerFactory.generateObjectContainer();
		
		if(m == null)
			return new PARSERObject();
		return m;
	}
	
	private List generateArrayContainer(ContainerFactory containerFactory){
		if(containerFactory == null)
			return new PARSERArray();
		List l = containerFactory.creatArrayContainer();
		
		if(l == null)
			return new PARSERArray();
		return l;
	}
	
	public void parse(String s, ContentManager contentManager) throws ParseRaiser {
		parse(s, contentManager, false);
	}
	
	public void parse(String s, ContentManager contentManager, boolean isResume) throws ParseRaiser {
		StringReader in=new StringReader(s);
		try{
			parse(in, contentManager, isResume);
		}
		catch(IOException ie){
			/*
			 * Actually it will never happen.
			 */
			throw new ParseRaiser(-1, ParseRaiser.ERROR_UNEXPECTED_EXCEPTION, ie);
		}
	}
	
	public void parse(Reader in, ContentManager contentManager) throws IOException, ParseRaiser {
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
	 * @throws ParseRaiser
	 */
	public void parse(Reader in, ContentManager contentManager, boolean isResume) throws IOException, ParseRaiser {
		if(!isResume){
            parseHome(in);
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
					contentManager.startPARSER();
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
						contentManager.endPARSER();
						status = S_END;
						return;
					}
					else{
                        parseAdviser();
					}
			
				case S_IN_OBJECT:
					nextToken();
					switch(token.type){
					case Yytoken.TYPE_COMMA:
						break;
					case Yytoken.TYPE_VALUE:
						if(token.value instanceof String){
                            if (parseAssist(contentManager, statusStack)) return;
                        }
						else{
                            parseHelper();
                        }
						break;
					case Yytoken.TYPE_RIGHT_BRACE:
						if(statusStack.size()>1){
                            new PARSERReaderEngine(statusStack).invoke();
						}
						else{
							status=S_IN_FINISHED_VALUE;
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
                            parseGuide(statusStack);
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
					throw new ParseRaiser(fetchPosition(), ParseRaiser.ERROR_UNEXPECTED_TOKEN, token);
				}//switch
				if(status==S_IN_ERROR){
					throw new ParseRaiser(fetchPosition(), ParseRaiser.ERROR_UNEXPECTED_TOKEN, token);
				}
			}while(token.type!=Yytoken.TYPE_EOF);
		}
		catch(IOException ie){
			status = S_IN_ERROR;
			throw ie;
		}
		catch(ParseRaiser pe){
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
		throw new ParseRaiser(fetchPosition(), ParseRaiser.ERROR_UNEXPECTED_TOKEN, token);
	}

    private void parseGuide(LinkedList statusStack) {
        statusStack.removeFirst();
        status=peekStatus(statusStack);
    }

    private void parseHelper() {
        status=S_IN_ERROR;
    }

    private boolean parseAssist(ContentManager contentManager, LinkedList statusStack) throws ParseRaiser, IOException {
        String key=(String)token.value;
        status=S_PASSED_PAIR_KEY;
        statusStack.addFirst(new Integer(status));
        if(!contentManager.startObjectEntry(key))
            return true;
        return false;
    }

    private void parseAdviser() throws ParseRaiser {
        status = S_IN_ERROR;
        throw new ParseRaiser(fetchPosition(), ParseRaiser.ERROR_UNEXPECTED_TOKEN, token);
    }

    private void parseHome(Reader in) {
        reset(in);
        managerStatusStack = new LinkedList();
    }

    private class PARSERReaderCoordinator {
        private LinkedList statusStack;
        private LinkedList valueStack;

        public PARSERReaderCoordinator(LinkedList statusStack, LinkedList valueStack) {
            this.statusStack = statusStack;
            this.valueStack = valueStack;
        }

        public void invoke() {
            String key=(String)token.value;
            valueStack.addFirst(key);
            status=S_PASSED_PAIR_KEY;
            statusStack.addFirst(new Integer(status));
        }
    }

    private class PARSERReaderEngine {
        private LinkedList statusStack;

        public PARSERReaderEngine(LinkedList statusStack) {
            this.statusStack = statusStack;
        }

        public void invoke() {
            statusStack.removeFirst();
            status=peekStatus(statusStack);
        }
    }
}
