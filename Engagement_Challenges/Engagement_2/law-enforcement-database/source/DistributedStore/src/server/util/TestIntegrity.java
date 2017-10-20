/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.util;

import index.BTree;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author user
 */
public class TestIntegrity {
    
    ArrayList<Integer> keysin;
    
    public void addVal(Integer key){
        if(keysin==null)
            keysin = new ArrayList<Integer>();
        keysin.add(key);
        
        Collections.sort(keysin);
    
    }
    
    public static void testorder(BTree btree){
        
        ArrayList<Integer> range = btree.getRange(0,1000000);
        
    
    
    }
    
}
