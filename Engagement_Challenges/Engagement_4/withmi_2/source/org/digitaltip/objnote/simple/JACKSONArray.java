/*
 * $Id: JSONArray.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-4-10
 */
package org.digitaltip.objnote.simple;

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
public class JACKSONArray extends ArrayList implements List, JACKSONAware, JSONStreamAware {
	private static final long serialVersionUID = 3957988303675231981L;

    /**
     * Encode a list into JSON text and write it to out. 
     * If this list is also a JSONStreamAware or a JSONAware, JSONStreamAware and JSONAware specific behaviours will be ignored at this top level.
     * 
     * @see JACKSONValue#writeJACKSONString(Object, Writer)
     * 
     * @param list
     * @param out
     */
	public static void writeJACKSONString(List list, Writer out) throws IOException{
		if(list == null){
            writeJACKSONStringService(out);
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
                new JACKSONArrayGuide(out).invoke();
                continue;
			}
			
			JACKSONValue.writeJACKSONString(value, out);
		}
		out.write(']');
	}

    private static void writeJACKSONStringService(Writer out) throws IOException {
        out.write("null");
        return;
    }

    public void writeJSONString(Writer out) throws IOException{
		writeJACKSONString(this, out);
	}
	
	/**
	 * Convert a list to JSON text. The result is a JSON array. 
	 * If this list is also a JSONAware, JSONAware specific behaviours will be omitted at this top level.
	 * 
	 * @see JACKSONValue#toJACKSONString(Object)
	 * 
	 * @param list
	 * @return JSON text, or "null" if list is null.
	 */
	public static String toJACKSONString(List list){
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
			sb.append(JACKSONValue.toJACKSONString(value));
		}
        sb.append(']');
		return sb.toString();
	}

	public String toJACKSONString(){
		return toJACKSONString(this);
	}
	
	public String toString() {
		return toJACKSONString();
	}


    private static class JACKSONArrayGuide {
        private Writer out;

        public JACKSONArrayGuide(Writer out) {
            this.out = out;
        }

        public void invoke() throws IOException {
            out.write("null");
            return;
        }
    }
}
