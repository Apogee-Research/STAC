/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.util;

import java.util.ArrayList;

/**
 *
 * @author user
 */
public class CheckRestrictedID {
    
    ArrayList<Integer> ids;
    
    public CheckRestrictedID(){
        ids = new ArrayList<Integer> ();
    
    }
    
    public void add(int id){
    ids.add(id);
    }
    
    public boolean isRestricted(int id){
    
        return ids.contains(id);
    
    }

    public boolean remove(Integer key) {
        return ids.remove(key);
    }
    
    
    
    
}
