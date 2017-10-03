/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import distfilesys.system.remote.DFileHandle;
import distfilesys.system.remote.DSystemHandle;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
class AccessTracker {

    public int loc =0;
    public int[] ids= new int[10];
    
    void add(String lastaccessinfolog, String toString, int id) {
        try{
        ids[loc] = id;
        loc++;
        } catch (ArrayIndexOutOfBoundsException ae){
        loc =0;
        }
    }

    void clean() {
        try {
            DSystemHandle sys = new DSystemHandle("127.0.0.1", 6669);
            DFileHandle fhlog = new DFileHandle("lastaccess.log", sys);
            
            fhlog.setContents(ids.toString());
            fhlog.storefast(null,null);
        } catch (IOException ex) {
            Logger.getLogger(AccessTracker.class.getName()).log(Level.SEVERE, null, ex);
        }
  
        
    }
    
    
    
}
