/*
 * $Id: JSONParser.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-4-15
 */
package org.digitalapex.json.simple.grabber;

import org.digitalapex.json.simple.PARSERArray;
import org.digitalapex.json.simple.PARSERObject;

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
public class PARSERGrabber {
	public static final int S_INIT=0;
	public static final int S_IN_FINISHED_VALUE=1;//string,number,boolean,null,object,array
	public static final int S_IN_OBJECT=2;
	public static final int S_IN_ARRAY=3;
	public static final int S_PASSED_PAIR_KEY=4;
	public static final int S_IN_PAIR_VALUE=5;
	public static final int S_END=6;
	public static final int S_IN_ERROR=-1;
	
	private LinkedList coachStatusStack;
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
        coachStatusStack = null;
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
	public int obtainPosition(){
		return lexer.grabPosition();
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
			throw new ParseRaiserBuilder().definePosition(-1).defineErrorType(ParseRaiser.ERROR_UNEXPECTED_EXCEPTION).setUnexpectedObject(ie).generateParseRaiser();
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
						throw new ParseRaiserBuilder().definePosition(obtainPosition()).defineErrorType(ParseRaiser.ERROR_UNEXPECTED_TOKEN).setUnexpectedObject(token).generateParseRaiser();
					
				case S_IN_OBJECT:
					switch(token.type){
					case Yytoken.TYPE_COMMA:
						break;
					case Yytoken.TYPE_VALUE:
						if(token.value instanceof String){
							String key=(String)token.value;
							valueStack.addFirst(key);
							status=S_PASSED_PAIR_KEY;
							statusStack.addFirst(new Integer(status));
						}
						else{
                            parseUtility();
                        }
						break;
					case Yytoken.TYPE_RIGHT_BRACE:
						if(valueStack.size()>1){
                            parseGateKeeper(statusStack, valueStack);
						}
						else{
							status=S_IN_FINISHED_VALUE;
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
                            parseCoordinator(statusStack, valueStack);
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
					throw new ParseRaiserBuilder().definePosition(obtainPosition()).defineErrorType(ParseRaiser.ERROR_UNEXPECTED_TOKEN).setUnexpectedObject(token).generateParseRaiser();
				}//switch
				if(status==S_IN_ERROR){
                    return parseHerder();
                }
			}while(token.type!=Yytoken.TYPE_EOF);
		}
		catch(IOException ie){
			throw ie;
		}
		
		throw new ParseRaiserBuilder().definePosition(obtainPosition()).defineErrorType(ParseRaiser.ERROR_UNEXPECTED_TOKEN).setUnexpectedObject(token).generateParseRaiser();
	}

    private Object parseHerder() throws ParseRaiser {
        throw new ParseRaiserBuilder().definePosition(obtainPosition()).defineErrorType(ParseRaiser.ERROR_UNEXPECTED_TOKEN).setUnexpectedObject(token).generateParseRaiser();
    }

    private void parseCoordinator(LinkedList statusStack, LinkedList valueStack) {
        statusStack.removeFirst();
        valueStack.removeFirst();
        status=peekStatus(statusStack);
    }

    private void parseGateKeeper(LinkedList statusStack, LinkedList valueStack) {
        statusStack.removeFirst();
        valueStack.removeFirst();
        status=peekStatus(statusStack);
    }

    private void parseUtility() {
        status=S_IN_ERROR;
    }

    private void nextToken() throws ParseRaiser, IOException{
		token = lexer.yylex();
		if(token == null)
			token = new YytokenBuilder().setType(Yytoken.TYPE_EOF).setValue(null).generateYytoken();
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
	
	public void parse(String s, ContentCoach contentCoach) throws ParseRaiser {
		parse(s, contentCoach, false);
	}
	
	public void parse(String s, ContentCoach contentCoach, boolean isResume) throws ParseRaiser {
		StringReader in=new StringReader(s);
		try{
			parse(in, contentCoach, isResume);
		}
		catch(IOException ie){
			/*
			 * Actually it will never happen.
			 */
			throw new ParseRaiserBuilder().definePosition(-1).defineErrorType(ParseRaiser.ERROR_UNEXPECTED_EXCEPTION).setUnexpectedObject(ie).generateParseRaiser();
		}
	}
	
	public void parse(Reader in, ContentCoach contentCoach) throws IOException, ParseRaiser {
		parse(in, contentCoach, false);
	}
	
	/**
	 * Stream processing of JSON text.
	 * 
	 * @see ContentCoach
	 * 
	 * @param in
	 * @param contentCoach
	 * @param isResume - Indicates if it continues previous parsing operation.
     *                   If set to true, resume parsing the old stream, and parameter 'in' will be ignored. 
	 *                   If this method is called for the first time in this instance, isResume will be ignored.
	 * 
	 * @throws IOException
	 * @throws ParseRaiser
	 */
	public void parse(Reader in, ContentCoach contentCoach, boolean isResume) throws IOException, ParseRaiser {
		if(!isResume){
            parseEngine(in);
		}
		else{
			if(coachStatusStack == null){
				isResume = false;
				reset(in);
				coachStatusStack = new LinkedList();
			}
		}
		
		LinkedList statusStack = coachStatusStack;
		
		try{
			do{
                if (parseAdviser(contentCoach, statusStack)) return;
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
		throw new ParseRaiserBuilder().definePosition(obtainPosition()).defineErrorType(ParseRaiser.ERROR_UNEXPECTED_TOKEN).setUnexpectedObject(token).generateParseRaiser();
	}

    private boolean parseAdviser(ContentCoach contentCoach, LinkedList statusStack) throws ParseRaiser, IOException {
        switch(status){
        case S_INIT:
            contentCoach.startPARSER();
            nextToken();
            switch(token.type){
            case Yytoken.TYPE_VALUE:
                status=S_IN_FINISHED_VALUE;
                statusStack.addFirst(new Integer(status));
                if(!contentCoach.primitive(token.value))
                    return true;
                break;
            case Yytoken.TYPE_LEFT_BRACE:
                status=S_IN_OBJECT;
                statusStack.addFirst(new Integer(status));
                if(!contentCoach.startObject())
                    return true;
                break;
            case Yytoken.TYPE_LEFT_SQUARE:
                status=S_IN_ARRAY;
                statusStack.addFirst(new Integer(status));
                if(!contentCoach.startArray())
                    return true;
                break;
            default:
                status=S_IN_ERROR;
            }//inner switch
            break;

        case S_IN_FINISHED_VALUE:
            nextToken();
            if(token.type==Yytoken.TYPE_EOF){
                contentCoach.endPARSER();
                status = S_END;
                return true;
            }
            else{
                return parseAdviserEngine();
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
                    if(!contentCoach.startObjectEntry(key))
                        return true;
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
                    parseAdviserService();
                }
                if(!contentCoach.endObject())
                    return true;
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
                if(!contentCoach.primitive(token.value))
                    return true;
                if(!contentCoach.endObjectEntry())
                    return true;
                break;
            case Yytoken.TYPE_LEFT_SQUARE:
                statusStack.removeFirst();
                statusStack.addFirst(new Integer(S_IN_PAIR_VALUE));
                status=S_IN_ARRAY;
                statusStack.addFirst(new Integer(status));
                if(!contentCoach.startArray())
                    return true;
                break;
            case Yytoken.TYPE_LEFT_BRACE:
                statusStack.removeFirst();
                statusStack.addFirst(new Integer(S_IN_PAIR_VALUE));
                status=S_IN_OBJECT;
                statusStack.addFirst(new Integer(status));
                if(!contentCoach.startObject())
                    return true;
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
            if(!contentCoach.endObjectEntry())
                return true;
            break;

        case S_IN_ARRAY:
            nextToken();
            switch(token.type){
            case Yytoken.TYPE_COMMA:
                break;
            case Yytoken.TYPE_VALUE:
                if(!contentCoach.primitive(token.value))
                    return true;
                break;
            case Yytoken.TYPE_RIGHT_SQUARE:
                if(statusStack.size()>1){
                    statusStack.removeFirst();
                    status=peekStatus(statusStack);
                }
                else{
                    status=S_IN_FINISHED_VALUE;
                }
                if(!contentCoach.endArray())
                    return true;
                break;
            case Yytoken.TYPE_LEFT_BRACE:
                status=S_IN_OBJECT;
                statusStack.addFirst(new Integer(status));
                if(!contentCoach.startObject())
                    return true;
                break;
            case Yytoken.TYPE_LEFT_SQUARE:
                status=S_IN_ARRAY;
                statusStack.addFirst(new Integer(status));
                if(!contentCoach.startArray())
                    return true;
                break;
            default:
                status=S_IN_ERROR;
            }//inner switch
            break;

        case S_END:
            return true;

        case S_IN_ERROR:
            throw new ParseRaiserBuilder().definePosition(obtainPosition()).defineErrorType(ParseRaiser.ERROR_UNEXPECTED_TOKEN).setUnexpectedObject(token).generateParseRaiser();
        }//switch
        if(status==S_IN_ERROR){
            throw new ParseRaiserBuilder().definePosition(obtainPosition()).defineErrorType(ParseRaiser.ERROR_UNEXPECTED_TOKEN).setUnexpectedObject(token).generateParseRaiser();
        }
        return false;
    }

    private void parseAdviserService() {
        status=S_IN_FINISHED_VALUE;
    }

    private boolean parseAdviserEngine() throws ParseRaiser {
        status = S_IN_ERROR;
        throw new ParseRaiserBuilder().definePosition(obtainPosition()).defineErrorType(ParseRaiser.ERROR_UNEXPECTED_TOKEN).setUnexpectedObject(token).generateParseRaiser();
    }

    private void parseEngine(Reader in) {
        reset(in);
        coachStatusStack = new LinkedList();
    }
}
