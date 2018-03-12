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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class BTreeTests1 {
 
        public static void main(String[] args) throws FileNotFoundException, IOException {

            try {
                BTReeMgrClient client = new BTReeMgrClient();
                
                client.login("picard");
                client.initSandbox();
                client.doSearchSBox( 0, 10000000);
                
                
                client.add(null, 600001, "");
                client.add(null, 600002, "");
                //System.out.println("add:"+add);
                client.add(null, 600003, "");
                //System.out.println("add:"+add);
                client.add(null, 600004, "");
                //System.out.println("add:"+add);
                //add = client.add(null, 600001);
                //System.out.println("add:"+add);
                client.doSearchSBox( 0, 10000000);
                
                client.doSearchMBox( "picard",0, 10000000);
                
                //client.rollback();
                client.commit();
                client.doSearchMBox( "picard",0, 10000000);
            } catch (CollabConnException ex) {
                Logger.getLogger(BTreeTests1.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        
    }
    
}
