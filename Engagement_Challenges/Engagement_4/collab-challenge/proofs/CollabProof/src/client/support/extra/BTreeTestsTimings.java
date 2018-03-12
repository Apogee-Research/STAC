/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.support.extra;

import client.support.BTReeMgrClient;
import client.support.CollabConnException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class BTreeTestsTimings {
 
        public static void main(String[] args) throws FileNotFoundException, IOException {

            List<Long> splits = new ArrayList<Long>();
            List<Long> nonsplits = new ArrayList<Long>();
          
            try {
                BTReeMgrClient client = new BTReeMgrClient();
                
                client.login("picard");
                client.initSandbox();
                
                //boolean add = client.add(null, 622322);
                //System.out.println("add:"+add);
                int start = 600004;
                int i=0;
                while(i <200){
                    client.add(null, start+i, "");
                    if(client.client.lastsplit){
                        splits.add(client.client.lasttiming);
                    } else {
                        nonsplits.add(client.client.lasttiming);
                    
                    }
                    
                    System.out.println("-----------------");
                    System.out.println("time:" + client.client.lasttiming);
                    i++;
                }
 
            } catch (CollabConnException ex) {
                Logger.getLogger(BTreeTestsTimings.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            Collections.sort(splits);
            Collections.sort(nonsplits);
            
            System.out.println("splits: "+splits.get(0) + ":" + splits.get(splits.size()-1));
            System.out.println("nonsplits: "+nonsplits.get(0) + ":" + nonsplits.get(nonsplits.size()-1));
        
        
    }
    
}
