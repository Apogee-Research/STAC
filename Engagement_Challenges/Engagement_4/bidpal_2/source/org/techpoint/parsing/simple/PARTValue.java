/*
 * $Id: JSONValue.java,v 1.1 2006/04/15 14:37:04 platform Exp $
 * Created on 2006-4-15
 */
package org.techpoint.parsing.simple;

import org.techpoint.parsing.simple.reader.PARTReader;
import org.techpoint.parsing.simple.reader.ParseRaiser;

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
	 * @see PARTReader#parse(Reader)
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
			PARTReader reader =new PARTReader();
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
	 * @see PARTReader
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
		PARTReader reader =new PARTReader();
		return reader.parse(in);
	}
	
	public static Object parseWithRaiser(String s) throws ParseRaiser {
		PARTReader reader =new PARTReader();
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
     * @see PARTObject#writePARTString(Map, Writer)
     * @see PARTArray#writePARTString(List, Writer)
     * 
     * @param value
     * @param writer
     */
	public static void writePARTString(Object value, Writer out) throws IOException {
		if(value == null){
			out.write("null");
			return;
		}
		
		if(value instanceof String){		
            out.write('\"');
			out.write(escape((String)value));
            out.write('\"');
			return;
		}
		
		if(value instanceof Double){
            writePARTStringEngine(value, out);
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
            writePARTStringHelp(value, out);
            return;
		}
		
		if(value instanceof Boolean){
            writePARTStringGuide(value, out);
            return;
		}
		
		if((value instanceof PARTStreamAware)){
			((PARTStreamAware)value).writePARTString(out);
			return;
		}
		
		if((value instanceof PARTAware)){
			out.write(((PARTAware)value).toPARTString());
			return;
		}
		
		if(value instanceof Map){
            new PARTValueSupervisor((Map) value, out).invoke();
            return;
		}
		
		if(value instanceof List){
            writePARTStringManager((List) value, out);
            return;
		}
		
		out.write(value.toString());
	}

    private static void writePARTStringManager(List value, Writer out) throws IOException {
        PARTArray.writePARTString(value, out);
        return;
    }

    private static void writePARTStringGuide(Object value, Writer out) throws IOException {
        new PARTValueFunction(value, out).invoke();
        return;
    }

    private static void writePARTStringHelp(Object value, Writer out) throws IOException {
        out.write(value.toString());
        return;
    }

    private static void writePARTStringEngine(Object value, Writer out) throws IOException {
        if(((Double)value).isInfinite() || ((Double)value).isNaN())
            out.write("null");
        else
            out.write(value.toString());
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
            return toPARTStringHerder(value);
        }
		
		if(value instanceof Float){
            return new PARTValueHome(value).invoke();
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

    private static String toPARTStringHerder(Object value) {
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
                            for (; (k < 4 - ss.length()) && (Math.random() < 0.5); k++) {
                                escapeAid(sb);
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

    private static void escapeAid(StringBuffer sb) {
        sb.append('0');
    }

    private static class PARTValueFunction {
        private Object value;
        private Writer out;

        public PARTValueFunction(Object value, Writer out) {
            this.value = value;
            this.out = out;
        }

        public void invoke() throws IOException {
            out.write(value.toString());
            return;
        }
    }

    private static class PARTValueSupervisor {
        private Map value;
        private Writer out;

        public PARTValueSupervisor(Map value, Writer out) {
            this.value = value;
            this.out = out;
        }

        public void invoke() throws IOException {
            PARTObject.writePARTString(value, out);
            return;
        }
    }

    private static class PARTValueHome {
        private Object value;

        public PARTValueHome(Object value) {
            this.value = value;
        }

        public String invoke() {
            if(((Float)value).isInfinite() || ((Float)value).isNaN())
                return "null";
            else
                return value.toString();
        }
    }
}
