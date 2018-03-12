/*
 * $Id: JSONObject.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-4-10
 */
package net.computerpoint.parsing.simple;


import net.computerpoint.dialogs.ProtocolsPublicIdentity;
import net.computerpoint.dialogs.ProtocolsNetworkAddress;
import net.computerpoint.dialogs.ProtocolsPublicIdentityBuilder;
import net.computerpoint.numerical.RsaPublicKey;

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
public class PARSERObject extends HashMap implements Map, PARSERAware, PARSERStreamAware {
	
	private static final long serialVersionUID = -503443796854799292L;
	
	
	public PARSERObject() {
		super();
	}

	/**
	 * Allows creation of a JSONObject from a Map. After that, both the
	 * generated JSONObject and the Map can be modified independently.
	 * 
	 * @param map
	 */
	public PARSERObject(Map map) {
		super(map);
	}


    /**
     * Encode a map into JSON text and write it to out.
     * If this map is also a JSONAware or JSONStreamAware, JSONAware or JSONStreamAware specific behaviours will be ignored at this top level.
     * 
     * @see PARSERValue#writePARSERString(Object, Writer)
     * 
     * @param map
     * @param out
     */
	public static void writePARSERString(Map map, Writer out) throws IOException {
		if(map == null){
			out.write("null");
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
			PARSERValue.writePARSERString(entry.getValue(), out);
		}
		out.write('}');
	}

    public ProtocolsPublicIdentity fromParser() {
        String id = (String) get("id");
        String callbackPlace = (String) get("callbackHost");
        long callbackPort = (long) get("callbackPort");
        RsaPublicKey publicKey = RsaPublicKey.fromJson((PARSERObject) get("publicKey"));

        return new ProtocolsPublicIdentityBuilder().setId(id).fixPublicKey(publicKey).assignCallbackAddress(new ProtocolsNetworkAddress(callbackPlace, (int) callbackPort)).formProtocolsPublicIdentity();
    }

    public void writePARSERString(Writer out) throws IOException{
		writePARSERString(this, out);
	}
	
	/**
	 * Convert a map to JSON text. The result is a JSON object. 
	 * If this map is also a JSONAware, JSONAware specific behaviours will be omitted at this top level.
	 * 
	 * @see PARSERValue#toPARSERString(Object)
	 * 
	 * @param map
	 * @return JSON text, or "null" if map is null.
	 */
	public static String toPARSERString(Map map){
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
			toPARSERString(String.valueOf(entry.getKey()), entry.getValue(), sb);
		}
        sb.append('}');
		return sb.toString();
	}
	
	public String toPARSERString(){
		return toPARSERString(this);
	}
	
	private static String toPARSERString(String key, Object value, StringBuffer sb){
		sb.append('\"');
        if(key == null)
            sb.append("null");
        else
            PARSERValue.escape(key, sb);
		sb.append('\"').append(':');
		
		sb.append(PARSERValue.toPARSERString(value));
		
		return sb.toString();
	}
	
	public String toString(){
		return toPARSERString();
	}

	public static String toString(String key,Object value){
        StringBuffer sb = new StringBuffer();
		toPARSERString(key, value, sb);
        return sb.toString();
	}
	
	/**
	 * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters (U+0000 through U+001F).
	 * It's the same as JSONValue.escape() only for compatibility here.
	 * 
	 * @see PARSERValue#escape(String)
	 * 
	 * @param s
	 * @return
	 */
	public static String escape(String s){
		return PARSERValue.escape(s);
	}
}
