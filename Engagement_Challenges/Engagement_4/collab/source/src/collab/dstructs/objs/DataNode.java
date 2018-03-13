/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package collab.dstructs.objs;

import collab.CollabRuntimeException;
import collab.SchedulingSandbox;
import java.util.List;
import collab.dstructs.objs.DataHolder;
import collab.dstructs.TreeHandler;
import collab.dstructs.TempIndexNode;
import collab.dstructs.TreeNodeCallBack;


/**
 *
 * @author user
 */
public class DataNode extends TempIndexNode implements TreeHandler {

    //THIS IS FIXED NUMBER OF SLOTS
    public static final int NodeMAX = 9;

      //private  int[] childids = null;
    public DataNode() {
        super();
        int[] childids = new int[NodeMAX];
        this.setUserObject(childids);

    }

    @Override
    public void takestep(SchedulingSandbox sb, List results, int low, int high, int dataindex, TreeNodeCallBack tncb) {

        int[] objs = (int[]) this.getUserObject();
        int data = objs[dataindex];
        if (data <= high && data >= low) {

            
            DataHolder dres = sb.thedata.get(data);
            if (dres != null && dres instanceof NormalUserData) {
                if(results.size()>2500){
                    System.out.println("That's enough! ERROR: Too many events, you must be a bot");
                    throw new CollabRuntimeException();
                }
                
                results.add(data);
            }

        }

    }

    @Override
    public void takestep(SchedulingSandbox sb,List results, int low, int high, TreeHandler walkerimpl, TreeNodeCallBack tncb) {
        super.takestep(sb,results, low, high, this, tncb);
    }
}
