/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package collab.dstructs;


import collab.dstructs.objs.DataNode;
import collab.dstructs.objs.MedianNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author user
 */
public class DotNodeCallBack implements TreeNodeCallBack {

    public Map<String, String> mappings;
    public List<String> medians;
    public List<String> datan;
    public List<String> relations;
    public static int outnum =0;
    
    public DotNodeCallBack() {
        mappings = new HashMap<String, String>();
        medians = new ArrayList<String>();
        datan = new ArrayList<String>();
        relations = new ArrayList<String>();
        outnum++;
    }

    @Override
    public void nodeCallBack(TempIndexNode tnode) {
        StringBuffer label = new StringBuffer();
        
            
        //System.out.println("tnode.uniqueid: " + tnode.uniqueid);
        int[] data = (int[])tnode.getUserObject();
        for(int i=0; i<data.length;i++){
            //System.out.println(":"+data[i]);
            label.append(":");
            label.append(data[i]);
        
        }
        mappings.put(Integer.toString(tnode.uniqueid), label.toString());
        
        if(tnode instanceof DataNode){
            //System.out.println("********************");
            datan.add(Integer.toString(tnode.uniqueid));

        } else 
        if(tnode instanceof MedianNode){
            //System.out.println("----");           
            medians.add(Integer.toString(tnode.uniqueid));
        }
        if(tnode.getParent()!=null){
         String rel = "" +tnode.getParent().uniqueid +"->"+tnode.uniqueid;
         relations.add(rel);
                
        } 
        
    
    }
    
/*digraph TrafficLights {
node [shape=box];  gy2; yr2; rg2; gy1; yr1; rg1;
node [shape=circle,fixedsize=true,width=0.9];  green2; yellow2; red2; safe2; safe1; green1; yellow1; red1;
gy2->yellow2;
rg2->green2;
yr2->safe1;
yr2->red2;
safe2->rg2;
green2->gy2;
yellow2->yr2;
red2->rg2;
gy1->yellow1;
rg1->green1;
yr1->safe2;
yr1->red1;
safe1->rg1;
green1->gy1;
yellow1->yr1;
red1->rg1;

overlap=false
label="PetriNet Model TrafficLights\nExtracted from ConceptBase and layed out by Graphviz"
fontsize=12;
}*/
}
