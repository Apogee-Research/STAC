/*
 * $Id: JSONArray.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-4-10
 */
package com.virtualpoint.part.simple;

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
public class PLUGINArray extends ArrayList implements List, PLUGINAware, PLUGINStreamAware {
	private static final long serialVersionUID = 3957988303675231981L;

    /**
     * Encode a list into JSON text and write it to out. 
     * If this list is also a JSONStreamAware or a JSONAware, JSONStreamAware and JSONAware specific behaviours will be ignored at this top level.
     * 
     * @see PLUGINValue#writePLUGINString(Object, Writer)
     * 
     * @param list
     * @param out
     */
	public static void writePLUGINString(List list, Writer out) throws IOException{
		if(list == null){
            writePLUGINStringGateKeeper(out);
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
			
			PLUGINValue.writePLUGINString(value, out);
		}
		out.write(']');
	}

    private static void writePLUGINStringGateKeeper(Writer out) throws IOException {
        out.write("null");
        return;
    }

    public void writePLUGINString(Writer out) throws IOException{
		writePLUGINString(this, out);
	}
	
	/**
	 * Convert a list to JSON text. The result is a JSON array. 
	 * If this list is also a JSONAware, JSONAware specific behaviours will be omitted at this top level.
	 * 
	 * @see PLUGINValue#toPLUGINString(Object)
	 * 
	 * @param list
	 * @return JSON text, or "null" if list is null.
	 */
	public static String toPLUGINString(List list){
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
                new PLUGINArrayUtility(sb).invoke();
                continue;
			}
			sb.append(PLUGINValue.toPLUGINString(value));
		}
        sb.append(']');
		return sb.toString();
	}

    public String toPLUGINString(){
		return toPLUGINString(this);
	}
	
	public String toString() {
		return toPLUGINString();
	}


    private static class PLUGINArrayUtility {
        private StringBuffer sb;

        public PLUGINArrayUtility(StringBuffer sb) {
            this.sb = sb;
        }

        public void invoke() {
            sb.append("null");
            return;
        }
    }
}
