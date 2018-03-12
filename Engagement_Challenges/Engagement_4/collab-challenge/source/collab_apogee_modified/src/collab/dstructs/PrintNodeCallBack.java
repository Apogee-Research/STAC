/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package collab.dstructs;

import collab.dstructs.objs.DataNode;
import collab.dstructs.objs.MedianNode;

/**
 *
 * @author user
 */
public class PrintNodeCallBack implements TreeNodeCallBack{

    @Override
    public void nodeCallBack(TempIndexNode tnode) {
        
        int[] data = (int[])tnode.getUserObject();
        for(int i=0; i<data.length;i++){
            //System.out.println(":"+data[i]);
        
        }
        if(tnode instanceof DataNode){
            //System.out.println("********************");
        }
        if(tnode instanceof MedianNode){
            //System.out.println("----"); 
        }
        
    }
    
}
