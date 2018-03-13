/*
MIT License

Copyright (c) 2017 Apogee Research

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class SCDataCollect {
    private static InetAddress host;
    private static int port;
    private static DatagramSocket socket;
    private static int LOGIN = 23;
    private static int INIT = 13;
    private static int COMMIT = 14;
    private static int INSERT = 3;
    private static int SEARCHMAIN = 10;
    private static int SEARCHSANDBOX = 11;
    private static int sessionID;
    private static int totalBytes = 0;

    private static void setupSocket() {
        try {
            host = InetAddress.getByName("127.0.0.1");
            socket = new DatagramSocket();
        } catch (UnknownHostException e) {
            System.out.println("Unknown Host: " + host);
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Unable to connect: " + host + ":" + port);
            System.exit(1);
        }
    }

    //termCond: 1 = num packets; 2 = termination packet
    private static boolean reachedTerm(int termCond, int term, int count, int serverOut) {
        if (termCond == 1) {
            return count == term;
        } else if (termCond == 2) {
            return serverOut == term;
        }
        return true;
    }

    private static ArrayList<List<String>> sendMany(ArrayList<DatagramPacket> dataList, int termCond, int term) {
        boolean done;
        long start;
        long end;
        int count;
        int serverOut;
        DatagramPacket retPacket;
        List<String> indData;
        ArrayList<List<String>> returnData = new ArrayList<>();
        try {
            for (DatagramPacket data : dataList) {
                start = System.nanoTime();
                socket.send(data);
                count = 0;
                indData = new ArrayList<>();
                done = false;
                while (!done) {
                    retPacket = new DatagramPacket(new byte[4], 4);
                    socket.receive(retPacket);
                    end = System.nanoTime();
                    serverOut = ByteBuffer.wrap(retPacket.getData()).getInt();
                    indData.add(Long.toString(end - start) + "_" + Integer.toString(serverOut));
                    count++;
                    done = reachedTerm(termCond, term, count, serverOut);
                }
                returnData.add(indData);
            }
        } catch (IOException e) {
            System.out.println("sendMany IO Error");
            System.exit(1);
        }
        return returnData;
    }

    private static void login() {
        setupSocket();
        String username = "picard";

        int bufferSize = 1 + (Integer.SIZE >> 3) + (username.length() * (Character.SIZE >> 3));
        ByteBuffer b = ByteBuffer.allocate(bufferSize);
        b.put((byte) LOGIN);
        b.putInt(username.length());
        for (int i = 0; i < username.length(); i++) {
            b.putChar(username.charAt(i));
        }

        DatagramPacket loginPacket = new DatagramPacket(b.array(), bufferSize, host, port);
        totalBytes += bufferSize;
        ArrayList<DatagramPacket> toSend = new ArrayList<>();
        toSend.add(loginPacket);
        ArrayList<List<String>> serverReturn = sendMany(toSend, 1, 2);
        System.out.println("Login: " + serverReturn.get(0).get(0) + " Len " + serverReturn.size());
        sessionID = Integer.parseInt(serverReturn.get(0).get(1).split("_")[1]);
        System.out.println("Session ID: " + sessionID);
    }

    private static void init() {
        int bufferSize = 1 + Integer.SIZE;
        ByteBuffer b = ByteBuffer.allocate(bufferSize);
        b.put((byte) INIT);
        b.putInt(sessionID);

        DatagramPacket initPacket = new DatagramPacket(b.array(), bufferSize, host, port);
        totalBytes += bufferSize;
        ArrayList<DatagramPacket> toSend = new ArrayList<>();
        toSend.add(initPacket);
        ArrayList<List<String>> serverReturn = sendMany(toSend, 1, 1);
        System.out.println("Init: " + serverReturn.get(0).get(0) + " Len " + serverReturn.size());
    }

    private static void commit() {
        int bufferSize = 1 + Integer.SIZE;
        ByteBuffer b = ByteBuffer.allocate(bufferSize);
        b.put((byte) COMMIT);
        b.putInt(sessionID);

        DatagramPacket commitPacket = new DatagramPacket(b.array(), bufferSize, host, port);
        totalBytes += bufferSize;
        ArrayList<DatagramPacket> toSend = new ArrayList<>();
        toSend.add(commitPacket);
        ArrayList<List<String>> serverReturn = sendMany(toSend, 1, 1);
        System.out.println("Commit: " + serverReturn.get(0).get(0) + " Len " + serverReturn.size());
    }

    private static ArrayList insert(int[] insertIDs) {
        int bufferSize = 9;
        ByteBuffer b;
        ArrayList<DatagramPacket> toSend = new ArrayList<>();

        for (int ID : insertIDs) {
            b = ByteBuffer.allocate(bufferSize);
            b.put((byte) INSERT);
            b.putInt(sessionID);
            b.putInt(ID);

            toSend.add(new DatagramPacket(b.array(), bufferSize, host, port));
            totalBytes += bufferSize;
        }

        ArrayList<List<String>> serverReturn = sendMany(toSend, 1, 2);
        ArrayList<Long> insertTimes = new ArrayList<>();

        int insertErrors = 0;
        String response;
        int status;
        long time;
        for (List<String> responses : serverReturn) {
            response = responses.get(0);
            status = Integer.parseInt(response.split("_")[1]);
            time = Long.parseLong(response.split("_")[0]);
            if (status != 1) {
                insertErrors++;
            }
            //System.out.println(response);
            insertTimes.add(time);
        }

        System.out.println("Insert: Len " + serverReturn.size() + " Errors: " + insertErrors);
        return insertTimes;
    }

    private static int searchSandbox(int startIND, int endIND) {
        int bufferSize = 13;
        ByteBuffer b = ByteBuffer.allocate(bufferSize);
        b.put((byte) SEARCHSANDBOX);
        b.putInt(sessionID);
        b.putInt(startIND);
        b.putInt(endIND);

        DatagramPacket searchPacket = new DatagramPacket(b.array(), bufferSize, host, port);
        totalBytes += bufferSize;
        ArrayList<DatagramPacket> toSend = new ArrayList<>();
        toSend.add(searchPacket);
        ArrayList<List<String>> serverReturn = sendMany(toSend, 2, -8);
        System.out.println("Search Sandbox: Len " + serverReturn.get(0).size() + " Time: " + serverReturn.get(0).get(serverReturn.get(0).size() - 1));

        return serverReturn.get(0).size();

    }

    private static void searchMain(int startIND, int endIND) {
        String username = "picard";
        int bufferSize = 1 + (Integer.SIZE >> 3) + (username.length() * (Character.SIZE >> 3)) + (Integer.SIZE >> 3) + (Integer.SIZE >> 3);
        ByteBuffer b = ByteBuffer.allocate(bufferSize);
        b.put((byte) SEARCHMAIN);
        b.putInt(username.length());
        for (int i = 0; i < username.length(); i++) {
            b.putChar(username.charAt(i));
        }
        b.putInt(startIND);
        b.putInt(endIND);

        DatagramPacket searchPacket = new DatagramPacket(b.array(), bufferSize, host, port);
        totalBytes += bufferSize;
        ArrayList<DatagramPacket> toSend = new ArrayList<>();
        toSend.add(searchPacket);
        ArrayList<List<String>> serverReturn = sendMany(toSend, 2, -8);

    }

    static void saveData(ArrayList<Long> dataIn, String trial) throws IOException {
        File file = new File("timeData" + trial + ".txt");
        file.createNewFile();
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
        for (long time : dataIn) {
            fileWriter.write(Long.toString(time) + "\n");
            fileWriter.flush();
        }
        fileWriter.close();
    }

    //static String
    public static void main(String[] args) throws IOException {
        String trial = "";
        if (args.length == 1) {
            trial = args[0];
        }
        host = null;
        port = 7688;
        socket = null;

        login();
        System.out.println("\tLogin, Total Bytes: " + totalBytes);
        init();
        System.out.println("\tInit, Total Bytes: " + totalBytes);

        System.out.println("");
        int search1 = searchSandbox(0, 40000000);
        System.out.println("\tSearch1, Total Bytes: " + totalBytes);
        System.out.println("");

        //Insert operations
        int[] insertIDs = new int[2400];
        for (int i = 0; i < insertIDs.length; i++) {
            insertIDs[i] = 200005 + i;
        }
        ArrayList<Long> insertTimes = insert(insertIDs);
        saveData(insertTimes, trial);
        System.out.println("\tInserts, Total Bytes: " + totalBytes);

        System.out.println("");
        int search2 = searchSandbox(0, 40000000);
        System.out.println("\tSearch2, Total Bytes: " + totalBytes);
        System.out.println("");

        commit();
        System.out.println("\tCommit, Total Bytes: " + totalBytes);
        System.out.println("");
        System.out.println("Search2 - Search1 = " + (search2 - search1));
    }
}
