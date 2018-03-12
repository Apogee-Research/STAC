/*
 * $Id: JSONValue.java,v 1.1 2006/04/15 14:37:04 platform Exp $
 * Created on 2006-4-15
 */
package edu.computerapex.json.simple;

import edu.computerapex.json.simple.parser.JSONRetriever;
import edu.computerapex.json.simple.parser.ParseDeviation;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.List;
import java.util.Map;


/**
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public class JSONValue {
	/**
	 * Parse JSON text into java object from the input source. 
	 * Please use parseWithException() if you don't want to ignore the exception.
	 * 
	 * @see JSONRetriever#parse(Reader)
	 * @see #parseWithDeviation(Reader)
	 * 
	 * @param in
	 * @return Instance of the following:
	 *	org.json.simple.JSONObject,
	 * 	org.json.simple.JSONArray,
	 * 	java.lang.String,
	 * 	java.lang.Number,
	 * 	java.lang.Boolean,
	 * 	null
	 * 
	 */
	public static Object parse(Reader in){
		try{
			JSONRetriever retriever =new JSONRetriever();
			return retriever.parse(in);
		}
		catch(Exception e){
			return null;
		}
	}
	
	public static Object parse(String s){
		StringReader in=new StringReader(s);
		return parse(in);
	}
	
	/**
	 * Parse JSON text into java object from the input source.
	 * 
	 * @see JSONRetriever
	 * 
	 * @param in
	 * @return Instance of the following:
	 * 	org.json.simple.JSONObject,
	 * 	org.json.simple.JSONArray,
	 * 	java.lang.String,
	 * 	java.lang.Number,
	 * 	java.lang.Boolean,
	 * 	null
	 * 
	 * @throws IOException
	 * @throws ParseDeviation
	 */
	public static Object parseWithDeviation(Reader in) throws IOException, ParseDeviation {
		JSONRetriever retriever =new JSONRetriever();
		return retriever.parse(in);
	}
	
	public static Object parseWithDeviation(String s) throws ParseDeviation {
		JSONRetriever retriever =new JSONRetriever();
		return retriever.parse(s);
	}
	
    /**
     * Encode an object into JSON text and write it to out.
     * <p>
     * If this object is a Map or a List, and it's also a JSONStreamAware or a JSONAware, JSONStreamAware or JSONAware will be considered firstly.
     * <p>
     * DO NOT call this method from writeJSONString(Writer) of a class that implements both JSONStreamAware and (Map or List) with 
     * "this" as the first parameter, use JSONObject.writeJSONString(Map, Writer) or JSONArray.writeJSONString(List, Writer) instead. 
     * 
     * @see JSONObject#writeJSONString(Map, Writer)
     * @see JSONArray#writeJSONString(List, Writer)
     * 
     * @param value
     * @param writer
     */
	public static void writeJSONString(Object value, Writer out) throws IOException {
		if(value == null){
            new JSONValueHelper(out).invoke();
            return;
		}
		
		if(value instanceof String){
            writeJSONStringUtility((String) value, out);
            return;
		}
		
		if(value instanceof Double){
            writeJSONStringEngine(value, out);
            return;
		}
		
		if(value instanceof Float){
            writeJSONStringFunction(value, out);
            return;
		}		
		
		if(value instanceof Number){
			out.write(value.toString());
			return;
		}
		
		if(value instanceof Boolean){
            writeJSONStringAdviser(value, out);
            return;
		}
		
		if((value instanceof JSONStreamAware)){
			((JSONStreamAware)value).writeJSONString(out);
			return;
		}
		
		if((value instanceof JSONAware)){
			out.write(((JSONAware)value).toJSONString());
			return;
		}
		
		if(value instanceof Map){
            new JSONValueService((Map) value, out).invoke();
            return;
		}
		
		if(value instanceof List){
			JSONArray.writeJSONString((List)value, out);
            return;
		}
		
		out.write(value.toString());
	}

    private static void writeJSONStringAdviser(Object value, Writer out) throws IOException {
        out.write(value.toString());
        return;
    }

    private static void writeJSONStringFunction(Object value, Writer out) throws IOException {
        if(((Float)value).isInfinite() || ((Float)value).isNaN())
            out.write("null");
        else
            out.write(value.toString());
        return;
    }

    private static void writeJSONStringEngine(Object value, Writer out) throws IOException {
        if(((Double)value).isInfinite() || ((Double)value).isNaN())
            out.write("null");
        else
            out.write(value.toString());
        return;
    }

    private static void writeJSONStringUtility(String value, Writer out) throws IOException {
        out.write('\"');
        out.write(escape(value));
        out.write('\"');
        return;
    }

    /**
	 * Convert an object to JSON text.
	 * <p>
	 * If this object is a Map or a List, and it's also a JSONAware, JSONAware will be considered firstly.
	 * <p>
	 * DO NOT call this method from toJSONString() of a class that implements both JSONAware and Map or List with 
	 * "this" as the parameter, use JSONObject.toJSONString(Map) or JSONArray.toJSONString(List) instead. 
	 * 
	 * @see JSONObject#toJSONString(Map)
	 * @see JSONArray#toJSONString(List)
	 * 
	 * @param value
	 * @return JSON text, or "null" if value is null or it's an NaN or an INF number.
	 */
	public static String toJSONString(Object value){
		if(value == null)
			return "null";
		
		if(value instanceof String)
			return "\""+escape((String)value)+"\"";
		
		if(value instanceof Double){
			if(((Double)value).isInfinite() || ((Double)value).isNaN())
				return "null";
			else
				return value.toString();
		}
		
		if(value instanceof Float){
			if(((Float)value).isInfinite() || ((Float)value).isNaN())
				return "null";
			else
				return value.toString();
		}		
		
		if(value instanceof Number)
			return value.toString();
		
		if(value instanceof Boolean)
			return value.toString();
		
		if((value instanceof JSONAware))
			return ((JSONAware)value).toJSONString();
		
		if(value instanceof Map)
			return JSONObject.toJSONString((Map)value);
		
		if(value instanceof List)
			return JSONArray.toJSONString((List)value);
		
		return value.toString();
	}

	/**
	 * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters (U+0000 through U+001F).
	 * @param s
	 * @return
	 */
	public static String escape(String s){
		if(s==null)
			return null;
        StringBuffer sb = new StringBuffer();
        escape(s, sb);
        return sb.toString();
    }

    /**
     * @param s - Must not be null.
     * @param sb
     */
    static void escape(String s, StringBuffer sb) {
		for(int c =0; c <s.length(); c++){
			char ch=s.charAt(c);
			switch(ch){
			case '"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '/':
				sb.append("\\/");
				break;
			default:
                //Reference: http://www.unicode.org/versions/Unicode5.1.0/
				if((ch>='\u0000' && ch<='\u001F') || (ch>='\u007F' && ch<='\u009F') || (ch>='\u2000' && ch<='\u20FF')){
					String ss=Integer.toHexString(ch);
					sb.append("\\u");
					for(int k=0;k<4-ss.length();k++){
						sb.append('0');
					}
					sb.append(ss.toUpperCase());
				}
				else{
                    escapeCoordinator(sb, ch);
                }
			}
		}//for
	}

    private static void escapeCoordinator(StringBuffer sb, char ch) {
        sb.append(ch);
    }

    private static class JSONValueHelper {
        private Writer out;

        public JSONValueHelper(Writer out) {
            this.out = out;
        }

        public void invoke() throws IOException {
            out.write("null");
            return;
        }
    }

    private static class JSONValueService {
        private Map value;
        private Writer out;

        public JSONValueService(Map value, Writer out) {
            this.value = value;
            this.out = out;
        }

        public void invoke() throws IOException {
            JSONObject.writeJSONString(value, out);
            return;
        }
    }
}
