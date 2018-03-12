/*
 * $Id: JSONObject.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-4-10
 */
package com.virtualpoint.part.simple;


import com.virtualpoint.numerical.CipherPublicKey;

import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A JSON object. Key value pairs are unordered. JSONObject supports java.util.Map interface.
 * 
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public class PLUGINObject extends HashMap implements Map, PLUGINAware, PLUGINStreamAware {
	
	private static final long serialVersionUID = -503443796854799292L;
	
	
	public PLUGINObject() {
		super();
	}

	/**
	 * Allows creation of a JSONObject from a Map. After that, both the
	 * generated JSONObject and the Map can be modified independently.
	 * 
	 * @param map
	 */
	public PLUGINObject(Map map) {
		super(map);
	}


    /**
     * Encode a map into JSON text and write it to out.
     * If this map is also a JSONAware or JSONStreamAware, JSONAware or JSONStreamAware specific behaviours will be ignored at this top level.
     * 
     * @see PLUGINValue#writePLUGINString(Object, Writer)
     * 
     * @param map
     * @param out
     */
	public static void writePLUGINString(Map map, Writer out) throws IOException {
		if(map == null){
            writePLUGINStringCoordinator(out);
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
			PLUGINValue.writePLUGINString(entry.getValue(), out);
		}
		out.write('}');
	}

    private static void writePLUGINStringCoordinator(Writer out) throws IOException {
        out.write("null");
        return;
    }

    public CipherPublicKey fromPlugin() {
        BigInteger divisor = new BigInteger((String) get("modulus"));
        BigInteger exponent = new BigInteger((String) get("exponent"));
        return new CipherPublicKey(divisor, exponent);
    }

    public void writePLUGINString(Writer out) throws IOException{
		writePLUGINString(this, out);
	}
	
	/**
	 * Convert a map to JSON text. The result is a JSON object. 
	 * If this map is also a JSONAware, JSONAware specific behaviours will be omitted at this top level.
	 * 
	 * @see PLUGINValue#toPLUGINString(Object)
	 * 
	 * @param map
	 * @return JSON text, or "null" if map is null.
	 */
	public static String toPLUGINString(Map map){
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
			toPLUGINString(String.valueOf(entry.getKey()), entry.getValue(), sb);
		}
        sb.append('}');
		return sb.toString();
	}
	
	public String toPLUGINString(){
		return toPLUGINString(this);
	}
	
	private static String toPLUGINString(String key, Object value, StringBuffer sb){
		sb.append('\"');
        if(key == null)
            sb.append("null");
        else
            PLUGINValue.escape(key, sb);
		sb.append('\"').append(':');
		
		sb.append(PLUGINValue.toPLUGINString(value));
		
		return sb.toString();
	}
	
	public String toString(){
		return toPLUGINString();
	}

	public static String toString(String key,Object value){
        StringBuffer sb = new StringBuffer();
		toPLUGINString(key, value, sb);
        return sb.toString();
	}
	
	/**
	 * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters (U+0000 through U+001F).
	 * It's the same as JSONValue.escape() only for compatibility here.
	 * 
	 * @see PLUGINValue#escape(String)
	 * 
	 * @param s
	 * @return
	 */
	public static String escape(String s){
		return PLUGINValue.escape(s);
	}
}
