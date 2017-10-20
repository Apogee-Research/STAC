/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dstore.app;

import dstore.app.DStoreClientMod;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author user
 */
public class CommandLineApp {

    public static DStoreClientMod btree = null;

    
    //TSTAC:HIS IS JUST A COMMAND LINE APP TO INTERACT WITH THE DATASTORE
    public static void main(String[] args) throws IOException {

        btree = new DStoreClientMod();
        btree.connect("127.0.0.1", DStoreClientMod.PORT);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (1 == 1) {
            System.out.print("Enter Command (SEARCH, INSERT, GET, PUT, DELETE, PUTRECORD,GETRECORD,HOST,DONE):");
            String cmd = br.readLine();

            switch (cmd) {

                case "HOST":
                    System.out.print("Enter Server IP:");
                    String host = br.readLine();
                    btree.connect(host, DStoreClientMod.PORT);
                    break;
                case "SEARCH":
                    doSearchID(br);
                    break;
                case "GET":
                    getFilename(br);
                    break;
                case "PUT":
                    put(br);

                    break;
                case "INSERT":
                    doInsertID(br);
                    break;
                case "DELETE":
                    doDeleteID(br);
                    break;
                case "PUTRECORD":
                    putRecord(br);
                    break;
                case "GETRECORD":
                    getRecord(br);
                    break;
                case "DONE":
                    System.exit(1);
                default:
                    System.out.println("Unknown command");
                    break;
            }
        }

    }

    public static void doInsertID(BufferedReader br) throws IOException {

        System.out.print("Enter ID to insert:");
        String id = br.readLine();
        try {
            int i = Integer.parseInt(id);

            btree.insertnewkey(i);
        } catch (NumberFormatException nfe) {
            System.err.println("Invalid Format!");
        }

    }

    public static void doSearchID(BufferedReader br) throws IOException {
        System.out.println("Enter ID  range to insert");
        System.out.println("min of range:");
        String minstr = br.readLine();
        System.out.println("max of range:");
        String maxstr = br.readLine();
        try {
            int min = Integer.parseInt(minstr);
            int max = Integer.parseInt(maxstr);

            ArrayList<Integer> search = btree.search(min, max);
            Iterator<Integer> it = search.iterator();
            while (it.hasNext()) {
                Integer next = it.next();
                System.out.println("\t" + next);

            }
            //ArrayList<Integer>
        } catch (NumberFormatException nfe) {
            System.err.println("Invalid Format!");
        }
    }

    public static void getFileNamefromID(BufferedReader br) throws IOException {
        System.out.print("Enter ID  to get associated File name:");
        System.out.print("ID:");
        String id = br.readLine();

        try {
            int idx = Integer.parseInt(id);

            //ArrayList<Integer>
        } catch (NumberFormatException nfe) {
            System.err.println("Invalid Format!");
        }
    }

    public static void doDeleteID(BufferedReader br) throws IOException {
        System.out.print("Enter ID  to delete:");
        System.out.print("ID:");
        String id = br.readLine();

        try {
            int i = Integer.parseInt(id);

            btree.delete(i);

        } catch (NumberFormatException nfe) {
            System.err.println("Invalid Format!");
        }
    }

    public static String getFilename(BufferedReader br) throws IOException {
        System.out.print("Enter ID  to get File record for:");
        System.out.print("ID:");
        String id = br.readLine();

        String val = null;
        try {
            int i = Integer.parseInt(id);
            val = btree.getval(i);
            System.out.println(val);

        } catch (NumberFormatException nfe) {
            System.err.println("Invalid Format!");
        }
        return val;

    }

    public static String getRecord(BufferedReader br) throws IOException {
        System.out.print("Enter Record Name:");

        String rname = br.readLine();

        String contents = btree.getfile(rname);

        System.out.println(contents);
        return rname;

    }

    public static void putRecord(BufferedReader br) throws IOException {

        System.out.print("Enter Record Name:");

        String rname = br.readLine();
        System.out.print("Enter Record Contents:");

        String contents = br.readLine();

        btree.storefile(rname, contents);

    }

    public static void put(BufferedReader br) throws IOException {

        System.out.print("Enter ID to update value of:");

        String idx = br.readLine();
        try {
            int id = Integer.parseInt(idx);

            System.out.print("Enter new val:");

            String contents = br.readLine();

            btree.update(id, contents);
        } catch (NumberFormatException nfe) {
            System.err.println("Invalid Format!");
        }

    }

}
