/*
 * $Id: JSONArray.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-4-10
 */
package net.roboticapex.parser.simple;

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
public class PARSINGArray extends ArrayList implements List, PARSINGAware, PARSINGStreamAware {
	private static final long serialVersionUID = 3957988303675231981L;

    /**
     * Encode a list into JSON text and write it to out. 
     * If this list is also a JSONStreamAware or a JSONAware, JSONStreamAware and JSONAware specific behaviours will be ignored at this top level.
     * 
     * @see PARSINGValue#writePARSINGString(Object, Writer)
     * 
     * @param list
     * @param out
     */
	public static void writePARSINGString(List list, Writer out) throws IOException{
		if(list == null){
            new PARSINGArrayService(out).invoke();
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
                writePARSINGStringHome(out);
                continue;
			}
			
			PARSINGValue.writePARSINGString(value, out);
		}
		out.write(']');
	}

    private static void writePARSINGStringHome(Writer out) throws IOException {
        out.write("null");
        return;
    }

    public void writePARSINGString(Writer out) throws IOException{
		writePARSINGString(this, out);
	}
	
	/**
	 * Convert a list to JSON text. The result is a JSON array. 
	 * If this list is also a JSONAware, JSONAware specific behaviours will be omitted at this top level.
	 * 
	 * @see PARSINGValue#toPARSINGString(Object)
	 * 
	 * @param list
	 * @return JSON text, or "null" if list is null.
	 */
	public static String toPARSINGString(List list){
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
                toPARSINGStringHome(sb);
                continue;
			}
			sb.append(PARSINGValue.toPARSINGString(value));
		}
        sb.append(']');
		return sb.toString();
	}

    private static void toPARSINGStringHome(StringBuffer sb) {
        new PARSINGArrayEntity(sb).invoke();
        return;
    }

    public String toPARSINGString(){
		return toPARSINGString(this);
	}
	
	public String toString() {
		return toPARSINGString();
	}


    private static class PARSINGArrayService {
        private Writer out;

        public PARSINGArrayService(Writer out) {
            this.out = out;
        }

        public void invoke() throws IOException {
            out.write("null");
            return;
        }
    }

    private static class PARSINGArrayEntity {
        private StringBuffer sb;

        public PARSINGArrayEntity(StringBuffer sb) {
            this.sb = sb;
        }

        public void invoke() {
            sb.append("null");
            return;
        }
    }
}
