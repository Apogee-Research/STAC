/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package collab.dstructs;

import collab.dstructs.objs.DataNode;
import collab.dstructs.objs.MedianNode;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author user
 */
public class GetRootNodeCallBack implements TreeNodeCallBack{

    public List<Integer> medians = new ArrayList<Integer>();
    public List<Integer> allvals = new ArrayList<Integer>();
    
    @Override
    public void nodeCallBack(TempIndexNode tnode) {

        if(tnode instanceof MedianNode){
            if(tnode.getParent()==null){
                int[] ids = (int[])tnode.getUserObject();
                for(int i=0; i<ids.length; i++){
                    medians.add(ids[i]);
                }
            }       
        }
        if(tnode instanceof DataNode){
            if(tnode.getParent()==null){
                int[] ids = (int[])tnode.getUserObject();
                for(int i=0; i<ids.length; i++){
                    allvals.add(ids[i]);
                }
            }
        
        }
    
    }
    
    
    
}
