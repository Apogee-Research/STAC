/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package collab.dstructs.objs;

import collab.dstructs.TreeHandler;
import collab.dstructs.TreeNodeCallBack;
import collab.dstructs.TempIndexNode;
import collab.SchedulingSandbox;
import java.util.List;

/**
 *
 * @author user
 */
public class MedianNode extends TempIndexNode implements TreeHandler{
    
          
    
      //private  int[] childids = null;
    
      
      public MedianNode(){
          super();
          int[] childids = new int[1];
          childids[0] = SchedulingSandbox.MAXPLUSONE;
          this.setUserObject(childids);
      
      }

    @Override
    public void takestep(SchedulingSandbox sb,List results, int low, int high, int dataindex, TreeNodeCallBack tncb) {
    
        int[] childids = (int[])this.getUserObject();
        int data = childids[dataindex];
        if(data == SchedulingSandbox.MAXPLUSONE)
            return;
        int datapeek =childids[dataindex + 1];
        
        if(high >= data && datapeek >= low){
        
            TempIndexNode[] cnodes =(TempIndexNode[])this.children();
            TempIndexNode cn = cnodes[dataindex];
            cn.takestep(sb, results,  low, high, null, tncb);
        }
        
    
    }
    
      @Override
    public void takestep(SchedulingSandbox sb,List results, int low, int high, TreeHandler walkerimpl, TreeNodeCallBack tncb) {
        super.takestep(sb,results, low, high, this, tncb);
    }
    
    
    
    
}
