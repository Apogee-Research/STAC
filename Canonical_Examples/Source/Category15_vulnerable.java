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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.TreeSet;
import java.util.concurrent.PriorityBlockingQueue;

//Vulnerability in callback from external library
//AC Time vulnerability in call from java.util.TreeSet to Category15_vulnerable.TSValue.compareTo
//Attacker can use budget to setup TreeSet backing data structure such that particular
//java.util.TreeSet.add() for benign user request exceeds resource usage limit
public class Category15_vulnerable {
    private static final int port = 8000;
    private static final int maxQueueSize = 1;
    private static ServerSocket server;
    private static Queue<CRequest> requestQueue;
    private static TreeSet<TSValue> processedValues;

    private static void startServer() {
        try {
            server = new ServerSocket(port);
            System.out.println("Server Started Port: " + port);
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
            while (true) {
                cRequest = requestQueue.peek();
                if (cRequest != null) {
                    client = cRequest.getClient();
                    out = new PrintWriter(client.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                    request = in.readLine();
                    // processedValues add new request to TreeSet of processed requests
                    // calls TSValue.compareTo() to determine where to place new value
                    processedValues.add(new TSValue(request));
                    out.println("Done");
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

    public static void main(String[] args) throws InterruptedException {
        requestQueue = new PriorityBlockingQueue<>();
        processedValues = new TreeSet<>();

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

    private static class TSValue implements Comparable<TSValue> {
        String myValue;

        public TSValue(String input){
            myValue = input;
        }

        @Override
        //compareTo takes 200 ms to perform operation
        public int compareTo(TSValue o) {
            try{
                Thread.sleep(200);
            }catch (InterruptedException e){
                System.out.println("Thread Interrupted");
            }
            return myValue.compareTo(o.myValue);
        }
    }
}
