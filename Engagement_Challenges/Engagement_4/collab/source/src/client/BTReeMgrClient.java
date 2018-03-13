/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class BTReeMgrClient {

    public DStoreClient client;
    private int sessionid;

    public BTReeMgrClient() {
        try {
            client = new DStoreClient();
            client.connect("127.0.0.1", DStoreClient.PORT);
        } catch (SocketException ex) {
            Logger.getLogger(BTReeMgrClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(BTReeMgrClient.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public int[] doSearchID(BufferedReader br, int minstr, int maxstr) throws CollabConnException {

        int[] vals = null;
        try {
            int min = minstr;
            int max = maxstr;

            ArrayList<Integer> search = client.search(sessionid, min, max);
            Iterator<Integer> it = search.iterator();
            while (it.hasNext()) {
                Integer next = it.next();
                System.out.println("\t" + next);

            }

            Integer[] toArray = search.toArray(new Integer[search.size()]);
            vals = new int[toArray.length];
            for (int i = 0; i < toArray.length; i++) {
                vals[i] = toArray[i];
            }
        } catch (NumberFormatException nfe) {
            System.err.println("Invalid Format!");
        } catch (IOException ex) {
            Logger.getLogger(BTReeMgrClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return vals;
    }

    public boolean add(BufferedReader br, int val, String desc) throws CollabConnException {

        boolean retv = false;
        try {

            retv = client.insertnewkeyfast(sessionid, val);

        } catch (NumberFormatException nfe) {
            System.err.println("Invalid Format!");
        } catch (IOException ex) {
            Logger.getLogger(BTReeMgrClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retv;
    }

    public boolean initTransaction(BufferedReader br) throws CollabConnException {

        boolean retv = false;
        try {

            retv = client.beginTransaction();
        } catch (NumberFormatException nfe) {
            System.err.println("Invalid Format!");
        } catch (IOException ex) {
            Logger.getLogger(BTReeMgrClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retv;
    }

    public void login(String picard) throws IOException, CollabConnException {
        sessionid = client.login(picard, "");
        if(sessionid > 0){
        System.out.println("You have successfully logged on! (sessionid:" + sessionid + ")");
        
        } else {
        System.out.println("Login failed!. (No user:" + picard + ")");

        }
    }

    public int[] doSearchSBox(int min, int max) throws CollabConnException {

        int[] vals = null;
        try {
            ArrayList<Integer> search = client.search(sessionid, min, max);
            /*Iterator<Integer> it = search.iterator();
            while (it.hasNext()) {
                Integer next = it.next();
                System.out.println("\t" + next);
            }*/

            Integer[] toArray = search.toArray(new Integer[search.size()]);
            vals = new int[toArray.length];
            for (int i = 0; i < toArray.length; i++) {
                vals[i] = toArray[i];
            }
        } catch (IOException ex) {
            Logger.getLogger(BTReeMgrClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return vals;
    }

    public int[] doSearchMBox(String uname, int min, int max) throws CollabConnException {

        int[] vals = null;
        try {
            ArrayList<Integer> search = client.searchmain(uname, min, max);
            /*Iterator<Integer> it = search.iterator();
            while (it.hasNext()) {
                Integer next = it.next();
                System.out.println("\t" + next);
            }*/

            Integer[] toArray = search.toArray(new Integer[search.size()]);
            vals = new int[toArray.length];
            for (int i = 0; i < toArray.length; i++) {
                vals[i] = toArray[i];
            }
        } catch (IOException ex) {
            Logger.getLogger(BTReeMgrClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return vals;
    }

    public void initSandbox() throws IOException, CollabConnException {
        client.initsbox(sessionid);

    }

    public void commit() throws IOException, CollabConnException {
        client.commit(sessionid);
    
    }
    
    public void rollback() throws IOException, CollabConnException {
        client.rollback(sessionid);
    
    }

}
