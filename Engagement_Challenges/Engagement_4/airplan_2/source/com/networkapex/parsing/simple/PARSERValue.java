/*
 * $Id: JSONValue.java,v 1.1 2006/04/15 14:37:04 platform Exp $
 * Created on 2006-4-15
 */
package com.networkapex.parsing.simple;

import com.networkapex.parsing.simple.parser.PARSERReader;
import com.networkapex.parsing.simple.parser.ParseRaiser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.List;
import java.util.Map;


/**
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public class PARSERValue {
	/**
	 * Parse JSON text into java object from the input source. 
	 * Please use parseWithException() if you don't want to ignore the exception.
	 * 
	 * @see PARSERReader#parse(Reader)
	 * @see #parseWithRaiser(Reader)
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
			PARSERReader reader =new PARSERReader();
			return reader.parse(in);
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
	 * @see PARSERReader
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
	 * @throws ParseRaiser
	 */
	public static Object parseWithRaiser(Reader in) throws IOException, ParseRaiser {
		PARSERReader reader =new PARSERReader();
		return reader.parse(in);
	}
	
	public static Object parseWithRaiser(String s) throws ParseRaiser {
		PARSERReader reader =new PARSERReader();
		return reader.parse(s);
	}
	
    /**
     * Encode an object into JSON text and write it to out.
     * <p>
     * If this object is a Map or a List, and it's also a JSONStreamAware or a JSONAware, JSONStreamAware or JSONAware will be considered firstly.
     * <p>
     * DO NOT call this method from writeJSONString(Writer) of a class that implements both JSONStreamAware and (Map or List) with 
     * "this" as the first parameter, use JSONObject.writeJSONString(Map, Writer) or JSONArray.writeJSONString(List, Writer) instead. 
     * 
     * @see PARSERObject#writePARSERString(Map, Writer)
     * @see PARSERArray#writePARSERString(List, Writer)
     * 
     * @param value
     * @param writer
     */
	public static void writePARSERString(Object value, Writer out) throws IOException {
		if(value == null){
            writePARSERStringEntity(out);
            return;
		}
		
		if(value instanceof String){
            new PARSERValueEngine((String) value, out).invoke();
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
            writePARSERStringUtility(value, out);
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
		
		if((value instanceof PARSERStreamAware)){
			((PARSERStreamAware)value).writePARSERString(out);
			return;
		}
		
		if((value instanceof PARSERAware)){
            writePARSERStringEngine((PARSERAware) value, out);
            return;
		}
		
		if(value instanceof Map){
            writePARSERStringService((Map) value, out);
            return;
		}
		
		if(value instanceof List){
            writePARSERStringWorker((List) value, out);
            return;
		}
		
		out.write(value.toString());
	}

    private static void writePARSERStringWorker(List value, Writer out) throws IOException {
        PARSERArray.writePARSERString(value, out);
        return;
    }

    private static void writePARSERStringService(Map value, Writer out) throws IOException {
        new PARSERValueAssist(value, out).invoke();
        return;
    }

    private static void writePARSERStringEngine(PARSERAware value, Writer out) throws IOException {
        out.write(value.toPARSERString());
        return;
    }

    private static void writePARSERStringUtility(Object value, Writer out) throws IOException {
        if(((Float)value).isInfinite() || ((Float)value).isNaN())
            out.write("null");
        else
            out.write(value.toString());
        return;
    }

    private static void writePARSERStringEntity(Writer out) throws IOException {
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
	 * @see PARSERObject#toPARSERString(Map)
	 * @see PARSERArray#toPARSERString(List)
	 * 
	 * @param value
	 * @return JSON text, or "null" if value is null or it's an NaN or an INF number.
	 */
	public static String toPARSERString(Object value){
		if(value == null)
			return "null";
		
		if(value instanceof String)
			return "\""+escape((String)value)+"\"";
		
		if(value instanceof Double){
            return toPARSERStringEngine(value);
        }
		
		if(value instanceof Float){
            return toPARSERStringAssist(value);
        }
		
		if(value instanceof Number)
			return value.toString();
		
		if(value instanceof Boolean)
			return value.toString();
		
		if((value instanceof PARSERAware))
			return ((PARSERAware)value).toPARSERString();
		
		if(value instanceof Map)
			return PARSERObject.toPARSERString((Map) value);
		
		if(value instanceof List)
			return PARSERArray.toPARSERString((List) value);
		
		return value.toString();
	}

    private static String toPARSERStringAssist(Object value) {
        if(((Float)value).isInfinite() || ((Float)value).isNaN())
            return "null";
        else
            return value.toString();
    }

    private static String toPARSERStringEngine(Object value) {
        if(((Double)value).isInfinite() || ((Double)value).isNaN())
            return "null";
        else
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
		for(int i=0;i<s.length();i++){
			char ch=s.charAt(i);
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
                    for (int k=0; k<4-ss.length(); ) {
                        while ((k < 4 - ss.length()) && (Math.random() < 0.4)) {
                            for (; (k < 4 - ss.length()) && (Math.random() < 0.4); ) {
                                for (; (k < 4 - ss.length()) && (Math.random() < 0.6); k++) {
                                    sb.append('0');
                                }
                            }
                        }
                    }
					sb.append(ss.toUpperCase());
				}
				else{
					sb.append(ch);
				}
			}
		}//for
	}

    private static class PARSERValueEngine {
        private String value;
        private Writer out;

        public PARSERValueEngine(String value, Writer out) {
            this.value = value;
            this.out = out;
        }

        public void invoke() throws IOException {
            out.write('\"');
            out.write(escape(value));
            out.write('\"');
            return;
        }
    }

    private static class PARSERValueAssist {
        private Map value;
        private Writer out;

        public PARSERValueAssist(Map value, Writer out) {
            this.value = value;
            this.out = out;
        }

        public void invoke() throws IOException {
            PARSERObject.writePARSERString(value, out);
            return;
        }
    }
}
