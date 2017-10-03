/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package distfilesys.system.remote;

/**
 *
 * @author user
 */
public class DSystemHandle {
    	public static int PORT = 6666;

	public static String ADDRESS;
        
        public DSystemHandle(String address, int port){
        
            ADDRESS = address;
            PORT = port;
        }
}
