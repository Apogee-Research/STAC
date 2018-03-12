/*
 * $Id: JSONArray.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-4-10
 */
package edu.cyberapex.parsing.simple;

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
public class PARTArray extends ArrayList implements List, PARTAware, PARTStreamAware {
	private static final long serialVersionUID = 3957988303675231981L;

    /**
     * Encode a list into JSON text and write it to out. 
     * If this list is also a JSONStreamAware or a JSONAware, JSONStreamAware and JSONAware specific behaviours will be ignored at this top level.
     * 
     * @see PARTValue#writePARTString(Object, Writer)
     * 
     * @param list
     * @param out
     */
	public static void writePARTString(List list, Writer out) throws IOException{
		if(list == null){
			out.write("null");
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
			
			PARTValue.writePARTString(value, out);
		}
		out.write(']');
	}
	
	public void writePARTString(Writer out) throws IOException{
		writePARTString(this, out);
	}
	
	/**
	 * Convert a list to JSON text. The result is a JSON array. 
	 * If this list is also a JSONAware, JSONAware specific behaviours will be omitted at this top level.
	 * 
	 * @see PARTValue#toPARTString(Object)
	 * 
	 * @param list
	 * @return JSON text, or "null" if list is null.
	 */
	public static String toPARTString(List list){
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
                toPARTStringHome(sb);
                continue;
			}
			sb.append(PARTValue.toPARTString(value));
		}
        sb.append(']');
		return sb.toString();
	}

    private static void toPARTStringHome(StringBuffer sb) {
        sb.append("null");
        return;
    }

    public String toPARTString(){
		return toPARTString(this);
	}
	
	public String toString() {
		return toPARTString();
	}

	
		
}
