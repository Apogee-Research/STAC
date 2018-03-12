/*
 * $Id: JSONValue.java,v 1.1 2006/04/15 14:37:04 platform Exp $
 * Created on 2006-4-15
 */
package com.techtip.json.simple;

import com.techtip.json.simple.retriever.PARTRetriever;
import com.techtip.json.simple.retriever.ParseDeviation;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.List;
import java.util.Map;


/**
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public class PARTValue {
	/**
	 * Parse JSON text into java object from the input source. 
	 * Please use parseWithException() if you don't want to ignore the exception.
	 * 
	 * @see PARTRetriever#parse(Reader)
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
			PARTRetriever retriever =new PARTRetriever();
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
	 * @see PARTRetriever
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
		PARTRetriever retriever =new PARTRetriever();
		return retriever.parse(in);
	}
	
	public static Object parseWithDeviation(String s) throws ParseDeviation {
		PARTRetriever retriever =new PARTRetriever();
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
     * @see PARTObject#writePARTString(Map, Writer)
     * @see PARTArray#writePARTString(List, Writer)
     * 
     * @param value
     * @param writer
     */
	public static void writePARTString(Object value, Writer out) throws IOException {
		if(value == null){
            writePARTStringGateKeeper(out);
            return;
		}
		
		if(value instanceof String){		
            out.write('\"');
			out.write(escape((String)value));
            out.write('\"');
			return;
		}
		
		if(value instanceof Double){
			if(((Double)value).isInfinite() || ((Double)value).isNaN())
				out.write("null");
			else
				out.write(value.toString());
			return;
		}
		
		if(value instanceof Float){
			if(((Float)value).isInfinite() || ((Float)value).isNaN())
				out.write("null");
			else
				out.write(value.toString());
			return;
		}		
		
		if(value instanceof Number){
			out.write(value.toString());
			return;
		}
		
		if(value instanceof Boolean){
			out.write(value.toString());
			return;
		}
		
		if((value instanceof JSONStreamAware)){
            writePARTStringAdviser((JSONStreamAware) value, out);
            return;
		}
		
		if((value instanceof PARTAware)){
            writePARTStringWorker((PARTAware) value, out);
            return;
		}
		
		if(value instanceof Map){
            writePARTStringHelper((Map) value, out);
            return;
		}
		
		if(value instanceof List){
            writePARTStringGuide((List) value, out);
            return;
		}
		
		out.write(value.toString());
	}

    private static void writePARTStringGuide(List value, Writer out) throws IOException {
        PARTArray.writePARTString(value, out);
        return;
    }

    private static void writePARTStringHelper(Map value, Writer out) throws IOException {
        PARTObject.writePARTString(value, out);
        return;
    }

    private static void writePARTStringWorker(PARTAware value, Writer out) throws IOException {
        out.write(value.toPARTString());
        return;
    }

    private static void writePARTStringAdviser(JSONStreamAware value, Writer out) throws IOException {
        value.writeJSONString(out);
        return;
    }

    private static void writePARTStringGateKeeper(Writer out) throws IOException {
        out.write("null");
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
	 * @see PARTObject#toPARTString(Map)
	 * @see PARTArray#toPARTString(List)
	 * 
	 * @param value
	 * @return JSON text, or "null" if value is null or it's an NaN or an INF number.
	 */
	public static String toPARTString(Object value){
		if(value == null)
			return "null";
		
		if(value instanceof String)
			return "\""+escape((String)value)+"\"";
		
		if(value instanceof Double){
            return new PARTValueSupervisor(value).invoke();
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
		
		if((value instanceof PARTAware))
			return ((PARTAware)value).toPARTString();
		
		if(value instanceof Map)
			return PARTObject.toPARTString((Map) value);
		
		if(value instanceof List)
			return PARTArray.toPARTString((List) value);
		
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
		for(int p =0; p <s.length(); p++){
			char ch=s.charAt(p);
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
                        escapeHelp(sb);
                    }
					sb.append(ss.toUpperCase());
				}
				else{
					sb.append(ch);
				}
			}
		}//for
	}

    private static void escapeHelp(StringBuffer sb) {
        sb.append('0');
    }

    private static class PARTValueSupervisor {
        private Object value;

        public PARTValueSupervisor(Object value) {
            this.value = value;
        }

        public String invoke() {
            if(((Double)value).isInfinite() || ((Double)value).isNaN())
                return "null";
            else
                return value.toString();
        }
    }
}
