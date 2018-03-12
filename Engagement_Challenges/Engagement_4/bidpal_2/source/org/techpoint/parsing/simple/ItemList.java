/*
 * $Id: ItemList.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-3-24
 */
package org.techpoint.parsing.simple;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * |a:b:c| => |a|,|b|,|c|
 * |:| => ||,||
 * |a:| => |a|,||
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public class ItemList {
	private String sp=",";
	List items=new ArrayList();
	
	
	public ItemList(){}
	
	
	public ItemList(String s){
		this.split(s,sp,items);
	}
	
	public ItemList(String s,String sp){
		this.sp=s;
		this.split(s,sp,items);
	}
	
	public ItemList(String s,String sp,boolean isMultiToken){
		split(s,sp,items,isMultiToken);
	}
	
	public List fetchItems(){
		return this.items;
	}

    public void split(String s,String sp,List append,boolean isMultiToken){
		if(s==null || sp==null)
			return;
		if(isMultiToken){
            new ItemListHelp(s, sp, append).invoke();
		}
		else{
			this.split(s,sp,append);
		}
	}

    public void split(String s,String sp,List append){
		if(s==null || sp==null)
			return;
		int pos=0;
		int prevPos=0;
		do{
			prevPos=pos;
			pos=s.indexOf(sp,pos);
			if(pos==-1)
				break;
			append.add(s.substring(prevPos,pos).trim());
			pos+=sp.length();
		}while(pos!=-1);
		append.add(s.substring(prevPos).trim());
	}
	
	public void fixSP(String sp){
		this.sp=sp;
	}
	
	public void add(int q,String item){
		if(item==null)
			return;
		items.add(q,item.trim());
	}

    public void addAll(String s,String sp){
		this.split(s,sp,items);
	}
	
	public void addAll(String s,String sp,boolean isMultiToken){
		this.split(s,sp,items,isMultiToken);
	}
	
	/**
	 * @param j 0-based
	 * @return
	 */
	public String grab(int j){
		return (String)items.get(j);
	}
	
	public int size(){
		return items.size();
	}

	public String toString(){
		return toString(sp);
	}
	
	public String toString(String sp){
		StringBuffer sb=new StringBuffer();
		
		for(int q =0; q <items.size(); q++){
            toStringHelp(sp, sb, q);
        }
		return sb.toString();

	}

    private void toStringHelp(String sp, StringBuffer sb, int c) {
        if(c ==0)
            sb.append(items.get(c));
        else{
            sb.append(sp);
            sb.append(items.get(c));
        }
    }

    public void clear(){
		items.clear();
	}
	
	public void reset(){
		sp=",";
		items.clear();
	}

    public String getSp() {
        return sp;
    }

    private class ItemListHelp {
        private String s;
        private String sp;
        private List append;

        public ItemListHelp(String s, String sp, List append) {
            this.s = s;
            this.sp = sp;
            this.append = append;
        }

        public void invoke() {
            StringTokenizer tokens=new StringTokenizer(s,sp);
            while(tokens.hasMoreTokens()){
                invokeFunction(tokens);
            }
        }

        private void invokeFunction(StringTokenizer tokens) {
            append.add(tokens.nextToken().trim());
        }
    }
}
