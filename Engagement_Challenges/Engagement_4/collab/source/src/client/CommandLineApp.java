/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import collab.CollabServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author user
 */
public class CommandLineApp {

    public static BTReeMgrClient clientint = null;

    public static boolean sboxexists = false;
    public static String uname = null;

    //TSTAC:HIS IS JUST A COMMAND LINE APP TO INTERACT WITH THE DATASTORE
    public static void main(String[] args) throws IOException, CollabConnException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        clientint = new BTReeMgrClient();

        System.out.println("WELCOME TO THE COLLAB EVENT MANAGEMENT SYSTEM");
        System.out.println("PLEASE LOGIN");
        System.out.println("Enter Username:");
        uname = br.readLine();
        try {
            login(uname);
        } catch (CollabConnException ex) {
            System.out.println("login failed");
            System.exit(-2);
        }
        while (1 == 1) {
            System.out.println("Enter Command (SEARCH, ADD, DONE, UNDO, QUIT):");
            String cmd = br.readLine();

            switch (cmd) {

                /*case "HOST":
                 System.out.print("Enter Server IP:");
                 String host = br.readLine();
                 clientint.connect(host, DStoreClient.PORT);
                 break;*/
                case "SEARCH":
                    doSearchID(br);
                    break;
                case "GET":
                    //getFilename(br);
                    break;
                case "PUT":
                    //put(br);

                    break;
                case "ADD":
                    doInsertID(br);
                    break;
                /*case "DELETE":
                 doDeleteID(br);
                 break;*/
                case "PUTRECORD":
                    //putRecord(br);
                    break;
                case "GETRECORD":
                    //getRecord(br);
                    break;
                case "DONE":
                    commit();
                    break;
                case "UNDO":
                    reject();
                    break;
                case "QUIT":
                    System.exit(1);
                    break;
                default:
                    System.out.println("Unknown command");
                    break;
            }
        }

    }

    public static void doInsertID(BufferedReader br) throws IOException {

        System.out.println("Enter event time to add (minimum value:" + CollabServer.MINVAL + ", max value: " + Integer.MAX_VALUE + "):");
        String id = br.readLine();
        try {
            if (!sboxexists) {
                clientint.initSandbox();
                sboxexists = true;
            }
            int i = Integer.parseInt(id);

            clientint.add(br, i, "");
        } catch (NumberFormatException nfe) {
            System.err.println("Invalid Format!");
        } catch (CollabConnException ex) {
            String message = ex.getMessage();
            if (message.contains("-5")) {
                System.out.println("DUPLICATE ID: ID ALREADY EXISTS, NOT ABLE TO ADD");
            } else if (message.contains("-2")) {
                System.out.println("INVALID VALUE, TOO SMALL");
            } else {
                System.out.println("SERVER ERROR");
            }
        }

    }

    public static void doSearchID(BufferedReader br) throws IOException {
        System.out.println("Enter event time range to search");
        System.out.println("min of range:");
        String minstr = br.readLine();
        System.out.println("max of range:");
        String maxstr = br.readLine();
        try {
            int min = Integer.parseInt(minstr);
            int max = Integer.parseInt(maxstr);

            int[] ids;
            if (sboxexists) {
                ids = clientint.doSearchSBox(min, max);
            } else {
                ids = clientint.doSearchMBox(uname, min, max);
            }
            List<Integer> idList = new ArrayList<Integer>();
            for (int index = 0; index < ids.length; index++) {
                idList.add(ids[index]);
            }
            Collections.sort(idList);
            Iterator<Integer> it = idList.iterator();
            while (it.hasNext()) {
                Integer next = it.next();
                System.out.println("\t" + next);

            }
            //ArrayList<Integer>
        } catch (NumberFormatException nfe) {
            System.err.println("Invalid Format!");
        } catch (CollabConnException ex) {
            System.err.println(ex.getMessage());
        }
    }

    private static void login(String uname) throws IOException, CollabConnException {

        if (uname.length() < 10) {
            clientint.login(uname);

        }

    }

    private static void commit() throws IOException, CollabConnException {
        if (sboxexists) {
            clientint.commit();
        }
        sboxexists = false;
    }

    private static void reject() throws IOException, CollabConnException {
        if (sboxexists) {
            clientint.rollback();
        }
        sboxexists = false;
    }

}
