/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import index.BTree;
import java.util.logging.Level;
import java.util.logging.Logger;
import static server.UDPServerHandler.IDMAX;
import static server.UDPServerHandler.IDMIN;

/**
 *
 * @author user
 */
public class Utils {

    //public static BTree btree;
    /*public static void main(String[] args) throws FileNotFoundException, IOException{
    
     //btree = new BTree(10);
     make();
     //backup(btree, "out.dump");
     //restore(btree, "out.dump");
     //backup(btree, "out.dump2");
     }*/
    public static String nameFile(int key) {
        String fname = key + ":" + System.currentTimeMillis();
        return fname;
    }

    /*public static void make() throws FileNotFoundException{
     PrintWriter outf = new PrintWriter("./dumps/setup.dump");
     Random R = new Random(231935532);//System.currentTimeMillis());
     int runs = 10000;
     for (int i = 0; i < runs; i += 1) {
     int m = R.nextInt(1000000) + 21700000;
     boolean issplit = btree.add(m, m, false);
     outf.println("ADD:" + m + ":" + nameFile(m)+"-0");
     //btree.onlyleafcansplit = true;
     //btree.beginTransaction();
     }
     int A = 290001;
     int B = 400000;
     int seed = 123;
     int min = 10;
     int max = A - 1;
     int num = 1001;

     R = new Random(seed);

     for (int x = 1; x <= num; x++) {

     int v = min + R.nextInt(max);

     //btree.beginTransaction();
     //System.out.println("rand v"+v);
     //btree.add(v, v, false);
     outf.println("ADD:" + v + ":" + nameFile(v)+"-0");
     //btree.commit();

     }
     //btree.add(374802, 374802, false);
     outf.println("ADD:" + 374802 + ":" + 374802+"-1");
  
     seed = 123;
     min = B+1;
     max = 600000;
     num = 1003;
     R = new Random(seed);

     for (int x = 1; x <= num; x++) {

     int v = min + R.nextInt(max);

     //btree.beginTransaction();
     //System.out.println("rand v"+v);
     //btree.add(v, v, false);
     outf.println("ADD:" + v + ":" + nameFile(v)+"-0");

     //btree.commit();

     }
     outf.flush();
     outf.close();
     }*/
    public static void backup(BTree btree, String bfile) throws FileNotFoundException {

        PrintWriter outf = new PrintWriter("./dumps/" + bfile);

        ArrayList<Integer> allKeys = btree.getRange(0, 2147483647);

        Iterator<Integer> it = allKeys.iterator();

        while (it.hasNext()) {
            Integer nextkey = it.next();
            outf.println("ADD:" + nextkey + ":" + nextkey);
            System.out.println("ADD:" + nextkey + ":" + nextkey);

        }
        outf.flush();
        outf.close();

    }

    public static void restore(BTree btree, String bfile, CheckRestrictedID restricted) throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader("./dumps/" + bfile));
        String line = null;

        while ((line = reader.readLine()) != null) {
            int indexOfKey = line.indexOf("ADD:") + "ADD:".length();
            int indexOfVal = line.indexOf(':', indexOfKey);
            int indexOfpermission = line.indexOf('-', indexOfKey);
            String keystr = line.substring(indexOfKey, indexOfVal);
            int key = Integer.parseInt(keystr);
            String vstr = line.substring(indexOfVal + 1, indexOfpermission);
            String perm = line.substring(indexOfpermission + 1, line.length());
            if (key >= IDMIN && key <= IDMAX) {

                int permv = Integer.parseInt(perm);
                if (permv > 0) {
                    restricted.add(key);
                }

                btree.add(key, vstr, false);
                System.out.println("log-"+line);
            /*try {
                    //Thread.sleep(5,100000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
                }*/
            } else {
                System.out.println("ERROR on "+key+", IDS Must fall in range:" + IDMIN + " to " + IDMAX);
                
            }
        }
        reader.close();

    }

    public static void rebalance(ArrayList<Integer> x) {

    }

}
