/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dstore.app;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;

/**
 *
 * @author user
 */
public class DatasetCreator {

    public static void main(String[] args) throws FileNotFoundException {

        //getlongestlocation(100000, 150000);
        //System.exit(1);

        //1000 10 100000 100000 20000000 3
        int numitems = 1000;//Integer.valueOf(args[0]);
        int numsecrets = 0;//Integer.valueOf(args[1]);
        int avgsizebetween = 250000;//Integer.valueOf(args[2]);

        int min = 100000;//Integer.valueOf(args[3]);
        int max = 20000000;//Integer.valueOf(args[4]);
        int seed = 3;//Integer.valueOf(args[5]);
        //int variance = Integer.valueOf(args[3]);

       // int ratio = numitems / numsecrets;
        PrintWriter dout = new PrintWriter("dataset.dump");
        Random r = new Random(seed);

        int val = min;
        int lastval = -1;
        int largestinterval = -1;
        int largestintervalbegin = -1;
        int largestintervalend = -1;
        for (int i = 0; i < numitems; i++) {

            int nextInt = r.nextInt(avgsizebetween);
            val += nextInt;
            //int nextsecret = r.nextInt(ratio*100);

            dout.print("ADD:" + val + ":" + val);

            /*if(nextsecret<100){
             System.out.println("-1");    
             }
             else*/
             dout.println("-0");
            if (lastval > -1) {
                int intv = val - lastval;
                if (intv > largestinterval) {
                    largestinterval = intv;
                    largestintervalbegin = lastval;
                    largestintervalend = val;
                }

            }

            lastval = val;

        }

        getlongestlocation(largestintervalbegin, largestintervalend, dout);
        //ADD:120084231:120084231-1
        //ADD:122010880:122010880-1
        //ADD:36579470:36579470-1
        dout.close();
    }

    public static int getlongestlocation(int largestintervalbegin, int largestintervalend, PrintWriter dout) {

        System.out.println("log:" + log2((largestintervalend - largestintervalbegin)));

        int steps = 0;
        int secret = -1;
        int range = largestintervalend - largestintervalbegin;
        int halfs = 1;
        while (halfs > 0) {
halfs = (range) / 2;
System.out.println("largestintervalbegin"+largestintervalbegin);
System.out.println("secret"+secret);
System.out.println("range"+range);
System.out.println("halfs"+halfs);
System.out.println("--------------");
            
            
            range = halfs;
            //if(steps==0)
            secret = largestintervalbegin + halfs;
            if((steps%2) == 0)
                largestintervalbegin = secret;


            
            
            steps++;
        }
        //secret = largestintervalbegin+secret;

        System.out.println("steps:" + steps + " secret:" + secret);
        
        dout.print("ADD:" + secret + ":" + secret);
         dout.println("-1");
        return secret;
    }

    public static int log2(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException();
        }
        return 31 - Integer.numberOfLeadingZeros(n);
    }

}
