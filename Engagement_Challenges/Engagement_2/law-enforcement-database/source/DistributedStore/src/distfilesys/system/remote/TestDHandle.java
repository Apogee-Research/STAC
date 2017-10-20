/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package distfilesys.system.remote;

import java.io.IOException;

/**
 *
 * @author user
 */
public class TestDHandle {
    
    public static void main(String[] args) throws IOException{
    
        DSystemHandle sys = new DSystemHandle("127.0.0.1", 6666);
        
        DFileHandle fh = new DFileHandle("config.security", sys);
        
       // fh.setContents("njknin");
        //fh.store();
        
        
        String retrieve = fh.retrieve();
        System.out.println(retrieve);
        
        
        
    
    }
    
}
