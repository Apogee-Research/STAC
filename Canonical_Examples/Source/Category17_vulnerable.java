/*MIT License

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
SOFTWARE.*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Collections.swap;

//Probablistic complexity function
public class Category17_vulnerable {
    private static final int port = 8000;
    private static final int maxQueueSize = 2;
    private static final int maxIntLength = 6;
    private static final int maxItems = 10000;
    private static final int minItems = 2;
    private static final char delimiter = ',';
    private static final double pMean = 1.2;
    private static final double pStDev = 0.1;
    private static ServerSocket server;
    private static Queue<CRequest> requestQueue;

    private static ArrayList<Item> parseInput(String s) throws IllegalArgumentException {
        StringBuilder sb = new StringBuilder();
        String tempS;
        Item tempItem;
        ArrayList<Item> list = new ArrayList<>();
        HashSet<String> duplicates = new HashSet<>();
        for (int i = 0; i < s.length(); i++) {
            switch (s.charAt(i)) {
                case delimiter:
                    tempS = sb.toString();
                    sb = new StringBuilder();
                    if (tempS.length() > maxIntLength) throw new IllegalArgumentException("Item Length Too Long");
                    if (tempS.equals("")) continue;
                    tempItem = new Item(tempS);
                    if (duplicates.add(tempS)) list.add(tempItem);
                    if (list.size() > maxItems) throw new IllegalArgumentException("Too Many Items");
                    break;
                default:
                    sb.append(s.charAt(i));
            }

        }
        if (list.size() < minItems) throw new IllegalArgumentException("Too Few Items");
        return list;
    }

    private static int getPivotIndex(int a, int b) {
        double pPivot = ThreadLocalRandom.current().nextGaussian() * pStDev + pMean;
        return a + Math.min(b - a, Math.max(0, Math.toIntExact(Math.round(pPivot * (b - a)))));
    }

    private static int partition(ArrayList<Item> items, int a, int b) {
        Item pivot = items.get(getPivotIndex(a, b));
        int i = a - 1;
        for (int j = a; j <= b - 1; j++) {
            if (items.get(j).compareTo(pivot) <= 0) { //items.get(j) <= pivot
                i++;
                swap(items, i, j);
            }
        }
        swap(items, i + 1, b);
        return i + 1;
    }

    private static void quickSort(ArrayList<Item> items, int a, int b) {
        if (a < b) {
            int m = partition(items, a, b);
            quickSort(items, a, m - 1);
            quickSort(items, m + 1, b);
        }
    }

    private static ArrayList<String> prepareOutput(ArrayList<Item> items) {
        ArrayList<String> toOutput = new ArrayList<>();
        for (Item item : items) {
            toOutput.add(item.toString());
        }
        return toOutput;
    }

    private static void startServer() {
        try {
            System.out.println("Server Started port: " + port);
            server = new ServerSocket(port);
            boolean queueStatus = true;
            Socket client;
            PrintWriter out;
            BufferedReader in;
            int order = 0;
            while (true) {
                order++;
                client = server.accept();
                //If request queue not full add request to queue
                if (requestQueue.size() <= maxQueueSize) {
                    queueStatus = requestQueue.offer(new CRequest(client, order));
                }
                //If request queue full return "Queue Full"
                if (requestQueue.size() > maxQueueSize || !queueStatus) {
                    out = new PrintWriter(client.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    in.readLine();
                    out.println("Queue Full");
                    client.shutdownOutput();
                    client.shutdownInput();
                    client.close();
                }
            }
        } catch (IOException e) {
            System.exit(-1);
        }
    }

    private static void processRequests() {
        System.out.println("Process Thread Started");
        try {
            CRequest cRequest;
            Socket client;
            PrintWriter out;
            BufferedReader in;
            String request;
            ArrayList<Item> itemList;
            while (true) {
                cRequest = requestQueue.peek();
                if (cRequest != null) {
                    client = cRequest.getClient();
                    out = new PrintWriter(client.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                    request = in.readLine();
                    try {
                        itemList = parseInput(request + ",");
                        quickSort(itemList, 0, itemList.size() - 1);
                        out.println(prepareOutput(itemList));
                    } catch (IllegalArgumentException e) {
                        out.println("Invalid input: " + e.getMessage());
                    }
                    requestQueue.poll();
                    client.shutdownOutput();
                    client.shutdownInput();
                    client.close();
                }
            }
        } catch (IOException e) {
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        requestQueue = new PriorityBlockingQueue<>();

        //Start Server Thread
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                startServer();
            }
        });
        t1.start();

        //Start Process Thread
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                processRequests();
            }
        });
        t2.start();
    }

    private static class Item {
        BigInteger myValue;

        private Item(String s) {
            myValue = new BigInteger(s);
        }

        private int compareTo(Item toCompare) {
            //Check equality (waste time)
            int min = Math.min(myValue.bitLength(), toCompare.myValue.bitLength());
            boolean equal = true;
            for (int x = 0; x < min; x++) {
                if (myValue.testBit(x) != toCompare.myValue.testBit(x)) {
                    equal = false;
                }
            }
            if (equal && myValue.bitLength() == toCompare.myValue.bitLength()) {
                return 0;
            }
            //Actual comparison
            return myValue.compareTo(toCompare.myValue);
        }

        public String toString() {
            return myValue.toString();
        }
    }

    private static class CRequest implements Comparable<CRequest> {
        private Socket myClient;
        private int myValue;

        public CRequest(Socket client, int value) {
            myClient = client;
            myValue = value;
        }

        public int getValue() {
            return myValue;
        }

        public Socket getClient() {
            return myClient;
        }

        @Override
        public int compareTo(CRequest ctr) {
            return Integer.compare(myValue, ctr.getValue());
        }
    }
}
