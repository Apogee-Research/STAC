/*
 * $Id: JSONObject.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-4-10
 */
package edu.networkcusp.jackson.simple;


import edu.networkcusp.math.CryptoPublicKey;
import edu.networkcusp.protocols.CommunicationsPublicIdentity;
import edu.networkcusp.protocols.CommunicationsNetworkAddress;

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
public class JACKSONObject extends HashMap implements Map, JACKSONAware, JACKSONStreamAware {
	
	private static final long serialVersionUID = -503443796854799292L;
	
	
	public JACKSONObject() {
		super();
	}

	/**
	 * Allows creation of a JSONObject from a Map. After that, both the
	 * generated JSONObject and the Map can be modified independently.
	 * 
	 * @param map
	 */
	public JACKSONObject(Map map) {
		super(map);
	}


    /**
     * Encode a map into JSON text and write it to out.
     * If this map is also a JSONAware or JSONStreamAware, JSONAware or JSONStreamAware specific behaviours will be ignored at this top level.
     * 
     * @see JACKSONValue#writeJACKSONString(Object, Writer)
     * 
     * @param map
     * @param out
     */
	public static void writeJACKSONString(Map map, Writer out) throws IOException {
		if(map == null){
            writeJACKSONStringGuide(out);
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
			JACKSONValue.writeJACKSONString(entry.getValue(), out);
		}
		out.write('}');
	}

    private static void writeJACKSONStringGuide(Writer out) throws IOException {
        new JACKSONObjectAdviser(out).invoke();
        return;
    }

    public CommunicationsPublicIdentity fromJackson() {
        String id = (String) get("id");
        String callbackHost = (String) get("callbackHost");
        long callbackPort = (long) get("callbackPort");
        CryptoPublicKey publicKey = CryptoPublicKey.fromJackson((JACKSONObject) get("publicKey"));

        return new CommunicationsPublicIdentity(id, publicKey, new CommunicationsNetworkAddress(callbackHost, (int)callbackPort));
    }

    public void writeJACKSONString(Writer out) throws IOException{
		writeJACKSONString(this, out);
	}
	
	/**
	 * Convert a map to JSON text. The result is a JSON object. 
	 * If this map is also a JSONAware, JSONAware specific behaviours will be omitted at this top level.
	 * 
	 * @see JACKSONValue#toJACKSONString(Object)
	 * 
	 * @param map
	 * @return JSON text, or "null" if map is null.
	 */
	public static String toJACKSONString(Map map){
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
			toJACKSONString(String.valueOf(entry.getKey()), entry.getValue(), sb);
		}
        sb.append('}');
		return sb.toString();
	}
	
	public String toJACKSONString(){
		return toJACKSONString(this);
	}
	
	private static String toJACKSONString(String key, Object value, StringBuffer sb){
		sb.append('\"');
        if(key == null)
            sb.append("null");
        else
            JACKSONValue.escape(key, sb);
		sb.append('\"').append(':');
		
		sb.append(JACKSONValue.toJACKSONString(value));
		
		return sb.toString();
	}
	
	public String toString(){
		return toJACKSONString();
	}

	public static String toString(String key,Object value){
        StringBuffer sb = new StringBuffer();
		toJACKSONString(key, value, sb);
        return sb.toString();
	}
	
	/**
	 * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters (U+0000 through U+001F).
	 * It's the same as JSONValue.escape() only for compatibility here.
	 * 
	 * @see JACKSONValue#escape(String)
	 * 
	 * @param s
	 * @return
	 */
	public static String escape(String s){
		return JACKSONValue.escape(s);
	}

    private static class JACKSONObjectAdviser {
        private Writer out;

        public JACKSONObjectAdviser(Writer out) {
            this.out = out;
        }

        public void invoke() throws IOException {
            out.write("null");
            return;
        }
    }
}
