/*
 * $Id: ItemList.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-3-24
 */
package net.roboticapex.parser.simple;

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
	
	public String[] fetchArray(){
		return (String[])this.items.toArray();
	}
	
	public void split(String s,String sp,List append,boolean isMultiToken){
		if(s==null || sp==null)
			return;
		if(isMultiToken){
			StringTokenizer tokens=new StringTokenizer(s,sp);
			while(tokens.hasMoreTokens()){
                splitAid(append, tokens);
            }
		}
		else{
			this.split(s,sp,append);
		}
	}

    private void splitAid(List append, StringTokenizer tokens) {
        new ItemListHerder(append, tokens).invoke();
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
	
	public void add(int b,String item){
		if(item==null)
			return;
		items.add(b,item.trim());
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
	 * @param p 0-based
	 * @return
	 */
	public String take(int p){
		return (String)items.get(p);
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
				sb.append(sp);
				sb.append(items.get(j));
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

    private class ItemListHerder {
        private List append;
        private StringTokenizer tokens;

        public ItemListHerder(List append, StringTokenizer tokens) {
            this.append = append;
            this.tokens = tokens;
        }

        public void invoke() {
            append.add(tokens.nextToken().trim());
        }
    }
}
