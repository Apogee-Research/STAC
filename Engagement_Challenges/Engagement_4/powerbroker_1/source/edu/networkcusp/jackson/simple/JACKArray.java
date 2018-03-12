/*
 * $Id: JSONArray.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-4-10
 */
package edu.networkcusp.jackson.simple;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * A JSON array. JSONObject supports java.util.List interface.
 * 
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public class JACKArray extends ArrayList implements List, JACKAware, JACKStreamAware {
	private static final long serialVersionUID = 3957988303675231981L;

    /**
     * Encode a list into JSON text and write it to out. 
     * If this list is also a JSONStreamAware or a JSONAware, JSONStreamAware and JSONAware specific behaviours will be ignored at this top level.
     * 
     * @see JACKValue#writeJACKString(Object, Writer)
     * 
     * @param list
     * @param out
     */
	public static void writeJACKString(List list, Writer out) throws IOException{
		if(list == null){
            writeJACKStringCoordinator(out);
            return;
		}
		
		boolean first = true;
		Iterator iter=list.iterator();
		
        out.write('[');
		while(iter.hasNext()){
            if(first)
                first = false;
            else
                out.write(',');
            
			Object value=iter.next();
			if(value == null){
				out.write("null");
				continue;
			}
			
			JACKValue.writeJACKString(value, out);
		}
		out.write(']');
	}

    private static void writeJACKStringCoordinator(Writer out) throws IOException {
        out.write("null");
        return;
    }

    public void writeJACKString(Writer out) throws IOException{
		writeJACKString(this, out);
	}
	
	/**
	 * Convert a list to JSON text. The result is a JSON array. 
	 * If this list is also a JSONAware, JSONAware specific behaviours will be omitted at this top level.
	 * 
	 * @see JACKValue#toJACKString(Object)
	 * 
	 * @param list
	 * @return JSON text, or "null" if list is null.
	 */
	public static String toJACKString(List list){
		if(list == null)
			return "null";
		
        boolean first = true;
        StringBuffer sb = new StringBuffer();
		Iterator iter=list.iterator();
        
        sb.append('[');
		while(iter.hasNext()){
            if(first)
                first = false;
            else
                sb.append(',');
            
			Object value=iter.next();
			if(value == null){
				sb.append("null");
				continue;
			}
			sb.append(JACKValue.toJACKString(value));
		}
        sb.append(']');
		return sb.toString();
	}

	public String toJACKString(){
		return toJACKString(this);
	}
	
	public String toString() {
		return toJACKString();
	}

	
		
}
