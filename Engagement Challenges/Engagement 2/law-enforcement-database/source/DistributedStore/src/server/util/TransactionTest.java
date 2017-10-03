/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.util;

import index.BTree;
import java.util.ArrayList;



/**
 *
 * @author user
 */
public class TransactionTest {
  
public static int PAGESIZE = 4;
    public static int FRONT = 0;
    public static int BACK = 1;
    public static int MIDDLE = 2;
    static BTree  thebtree;
    
    public static ArrayList<Integer> thekeys = new ArrayList<Integer>();
    public static ArrayList<Integer> uncommittedkeys = new ArrayList<Integer>();
    
    public static void addtillN(int addval, int n, int frontbackmiddle) {

        for (int i = 0; i < n; i++) {
            add(addval, addval);
            if (frontbackmiddle == BACK) {
                addval--;
            }
            if (frontbackmiddle == FRONT || frontbackmiddle == MIDDLE) {
                addval++;
            }
        }

    }
    
    public static boolean add(int k, int v) {

        boolean splitornot = thebtree.add(k, v, true);
        uncommittedkeys.add(k);
        return splitornot;
    }
    
    
    public static void main(String[] args){
    
        thebtree = new BTree(PAGESIZE);
    
        ////System.out.println("1.1");
        ////TestHarness2.addtillNSkip(10,10,10,FRONT);
        //thebtree.printOutWholetreeX(-1);
        thebtree.optimizedinserts = true;
        ////thebtree.beginTransaction();
        ////TestHarness2.addtillSplit(101,200,FRONT);
        ////System.out.println("2.1");

        //thebtree.printOutWholetreeX(-1);
        
        //thebtree.searchForNode(101);
        ////thebtree.rollback();
                System.out.println("1.1");

                addtillN(102646,1,FRONT);
                
                
        thebtree.beginTransaction();
        addtillN(100000,15,FRONT);
                System.out.println("2.1");

        //thebtree.printOutWholetreeX(100003);
        
        thebtree.rollback();
                System.out.println("3.1");  
        //thebtree.printOutWholetreeX(-1);
        
        
    }
    
    
}
