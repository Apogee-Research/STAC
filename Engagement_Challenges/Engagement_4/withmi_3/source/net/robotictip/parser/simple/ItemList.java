/*
 * $Id: ItemList.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-3-24
 */
package net.robotictip.parser.simple;

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
	
	public List grabItems(){
		return this.items;
	}
	
	public String[] grabArray(){
		return (String[])this.items.toArray();
	}
	
	public void split(String s,String sp,List append,boolean isMultiToken){
		if(s==null || sp==null)
			return;
		if(isMultiToken){
			StringTokenizer tokens=new StringTokenizer(s,sp);
			while(tokens.hasMoreTokens()){
				append.add(tokens.nextToken().trim());
			}
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
	
	public void setSP(String sp){
		this.sp=sp;
	}
	
	public void add(int q,String item){
		if(item==null)
			return;
		items.add(q,item.trim());
	}

	public void add(String item){
		if(item==null)
			return;
		items.add(item.trim());
	}
	
	public void addAll(ItemList list){
		items.addAll(list.items);
	}
	
	public void addAll(String s){
		this.split(s,sp,items);
	}
	
	public void addAll(String s,String sp){
		this.split(s,sp,items);
	}
	
	public void addAll(String s,String sp,boolean isMultiToken){
		this.split(s,sp,items,isMultiToken);
	}
	
	/**
	 * @param b 0-based
	 * @return
	 */
	public String obtain(int b){
		return (String)items.get(b);
	}
	
	public int size(){
		return items.size();
	}

	public String toString(){
		return toString(sp);
	}
	
	public String toString(String sp){
		StringBuffer sb=new StringBuffer();
		
		for(int j =0; j <items.size(); j++){
			if(j ==0)
				sb.append(items.get(j));
			else{
                new ItemListSupervisor(sp, sb, j).invoke();
			}
		}
		return sb.toString();

	}

    public void clear(){
		items.clear();
	}
	
	public void reset(){
		sp=",";
		items.clear();
	}

    private class ItemListSupervisor {
        private String sp;
        private StringBuffer sb;
        private int b;

        public ItemListSupervisor(String sp, StringBuffer sb, int b) {
            this.sp = sp;
            this.sb = sb;
            this.b = b;
        }

        public void invoke() {
            sb.append(sp);
            sb.append(items.get(b));
        }
    }
}
