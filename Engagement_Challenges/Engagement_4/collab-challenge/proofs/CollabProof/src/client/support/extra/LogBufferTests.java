/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.support.extra;



/**
 *
 * @author user
 */
public class LogBufferTests {
    
        public static void main(String[] args) {

        LogBuffer lb = new LogBuffer(null);

        for (int i = 0; i < 1000; i++) {
            //java.util.logging.LogRecord lr = new java.util.logging.LogRecord(java.util.logging.Level.INFO, "aa");

            long nanoTime1 = System.nanoTime();

            lb.publish("aa");
            long nanoTime2 = System.nanoTime();
            System.out.println(" :" + (nanoTime2 - nanoTime1));
        }

    }
    
}
