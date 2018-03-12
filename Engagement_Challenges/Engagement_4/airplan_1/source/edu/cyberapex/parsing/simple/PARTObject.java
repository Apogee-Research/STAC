/*
 * $Id: JSONObject.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-4-10
 */
package edu.cyberapex.parsing.simple;


import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A JSON object. Key value pairs are unordered. JSONObject supports java.util.Map interface.
 * 
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public class PARTObject extends HashMap implements Map, PARTAware, PARTStreamAware {
	
	private static final long serialVersionUID = -503443796854799292L;
	
	
	public PARTObject() {
		super();
	}

	/**
	 * Allows creation of a JSONObject from a Map. After that, both the
	 * generated JSONObject and the Map can be modified independently.
	 * 
	 * @param map
	 */
	public PARTObject(Map map) {
		super(map);
	}


    /**
     * Encode a map into JSON text and write it to out.
     * If this map is also a JSONAware or JSONStreamAware, JSONAware or JSONStreamAware specific behaviours will be ignored at this top level.
     * 
     * @see PARTValue#writePARTString(Object, Writer)
     * 
     * @param map
     * @param out
     */
	public static void writePARTString(Map map, Writer out) throws IOException {
		if(map == null){
            writePARTStringWorker(out);
            return;
		}
		
		boolean first = true;
		Iterator iter=map.entrySet().iterator();
		
        out.write('{');
		while(iter.hasNext()){
            if(first)
                first = false;
            else
                out.write(',');
			Map.Entry entry=(Map.Entry)iter.next();
            out.write('\"');
            out.write(escape(String.valueOf(entry.getKey())));
            out.write('\"');
            out.write(':');
			PARTValue.writePARTString(entry.getValue(), out);
		}
		out.write('}');
	}

    private static void writePARTStringWorker(Writer out) throws IOException {
        out.write("null");
        return;
    }

    public void writePARTString(Writer out) throws IOException{
		writePARTString(this, out);
	}
	
	/**
	 * Convert a map to JSON text. The result is a JSON object. 
	 * If this map is also a JSONAware, JSONAware specific behaviours will be omitted at this top level.
	 * 
	 * @see PARTValue#toPARTString(Object)
	 * 
	 * @param map
	 * @return JSON text, or "null" if map is null.
	 */
	public static String toPARTString(Map map){
		if(map == null)
			return "null";
		
        StringBuffer sb = new StringBuffer();
        boolean first = true;
		Iterator iter=map.entrySet().iterator();
		
        sb.append('{');
		while(iter.hasNext()){
            if(first)
                first = false;
            else
                sb.append(',');
            
			Map.Entry entry=(Map.Entry)iter.next();
			toPARTString(String.valueOf(entry.getKey()), entry.getValue(), sb);
		}
        sb.append('}');
		return sb.toString();
	}
	
	public String toPARTString(){
		return toPARTString(this);
	}
	
	private static String toPARTString(String key, Object value, StringBuffer sb){
		sb.append('\"');
        if(key == null)
            sb.append("null");
        else
            PARTValue.escape(key, sb);
		sb.append('\"').append(':');
		
		sb.append(PARTValue.toPARTString(value));
		
		return sb.toString();
	}
	
	public String toString(){
		return toPARTString();
	}

	public static String toString(String key,Object value){
        StringBuffer sb = new StringBuffer();
		toPARTString(key, value, sb);
        return sb.toString();
	}
	
	/**
	 * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters (U+0000 through U+001F).
	 * It's the same as JSONValue.escape() only for compatibility here.
	 * 
	 * @see PARTValue#escape(String)
	 * 
	 * @param s
	 * @return
	 */
	public static String escape(String s){
		return PARTValue.escape(s);
	}
}
