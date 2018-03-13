/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package infotraderproof;

import java.io.IOException;

/**
 *
 * @author user
 */
public class Main {
    public static void main(String[] args) throws IOException{
    
        if(args.length > 0){
        
            String req = args[0];
            if(req.equalsIgnoreCase("redherring")){
            RunRedHerring.main(null);
            }
        } else {
            RunProof.main(null);
        }
        
    }
}
