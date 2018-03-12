/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package collab.dstructs;

import collab.SchedulingSandbox;
import java.util.List;

/**
 *
 * @author user
 */
public interface Walkable {
    
    public void takestep( SchedulingSandbox sb,List results, int low, int high, TreeHandler walkerimpl, TreeNodeCallBack tncb);
    
}
