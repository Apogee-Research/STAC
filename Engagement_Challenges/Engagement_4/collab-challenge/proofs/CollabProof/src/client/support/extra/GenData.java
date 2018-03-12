/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.support.extra;


import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 *
 * @author user
 */
public class GenData {
    
    
    public static void main(String[] args) throws FileNotFoundException{
    
        PrintWriter dataout = new PrintWriter("src/data/event.data");
        
        printudata(dataout, "picard");
        printudata(dataout, "janeway");
        printudata(dataout, "kirk");
        printudata(dataout, "cisco");

        dataout.flush();
        dataout.close();
        
        PrintWriter userout = new PrintWriter("src/data/user.data");
        printusersFile(userout, "picard", 1);
        printusersFile(userout, "janeway", 2);
        printusersFile(userout, "kirk", 3);
        printusersFile(userout, "cisco", 4);
        userout.flush();
        userout.close();
        
        PrintWriter auditout = new PrintWriter("src/data/audit.data");
        printAuditorFile(auditout,"picard" , 622448);
        auditout.flush();
        auditout.close();
        
       
    }
    
    public static void printudata(PrintWriter dataout, String user){
        
        int size = 60;
        int[] keys = new int[size];
        for (int i = 1; i <= size; i++) {
            keys[i - 1] = i * 100000;
            dataout.println(user +","+ keys[i - 1]);

        }
    }
    
    public static void printusersFile(PrintWriter dataout,String user, int id){
        
        dataout.println(user +","+ id);
        
    
    }
    
    public static void printAuditorFile(PrintWriter dataout,String user, int time){
        dataout.println(user +","+ time);
    
    }
    
}
