/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package collab.dstructs;

import collab.CollabRuntimeException;
import collab.SchedulingSandbox;
import collab.dstructs.TreeNodeCallBack;
import collab.dstructs.Walkable;
import java.util.List;

/**
 *
 * @author user
 */
public interface TreeHandler extends Walkable{
    
    public void takestep(SchedulingSandbox sb, List results, int low, int high, int dataindex, TreeNodeCallBack tncb);
    
}
