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
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.Queue;

//Insufficient sampling of input space could result in missed
//AC vulnerability
public class Category13_vulnerable {
    private static final int port = 8000;
    private static final int maxQueueSize = 2;
    private static final int maxInputLen = 3000;
    private static final BigDecimal minInput = new BigDecimal("0");
    private static final BigDecimal maxInput = new BigDecimal("1E100");
    private static final BigDecimal M = new BigDecimal("100");
    private static final BigDecimal E = new BigDecimal("1");
    private static final MathContext mc = new MathContext(10000);
    private static ServerSocket server;
    private static Queue<CRequest> requestQueue;

    private static BigDecimal function(BigDecimal x) {
        BigDecimal result = x.subtract(M, mc).pow(2, mc).subtract(E, mc);
        return result;
    }

    private static BigDecimal derivative(BigDecimal x) {
        BigDecimal result = x.subtract(M, mc).multiply(new BigDecimal("2"), mc);
        return result;
    }

    private static BigDecimal nextX(BigDecimal x) {
        BigDecimal result = x.subtract(function(x).divide(derivative(x), mc), mc);
        return result;
    }

    private static int newtonMethod(BigDecimal xInit) {
        BigDecimal stopDistance = new BigDecimal("1E-100");
        BigDecimal stopFunction = new BigDecimal("1E-100");
        int n = 1;
        BigDecimal xPrevious;
        BigDecimal xCurrent = xInit;
        do {
            xPrevious = xCurrent;
            xCurrent = nextX(xPrevious);
            n++;
        } while (xCurrent.subtract(xPrevious, mc).abs().compareTo(stopDistance) > 0 &&
                function(xCurrent).abs().compareTo(stopFunction) > 0);
        return n;
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
            BigDecimal xIn;
            int n;
            while (true) {
                cRequest = requestQueue.peek();
                if (cRequest != null) {
                    client = cRequest.getClient();
                    out = new PrintWriter(client.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                    request = in.readLine();
                    if (request.length() < maxInputLen && !request.toLowerCase().contains("e")) {
                        try {
                            xIn = new BigDecimal(request);
                            if (xIn.compareTo(maxInput) > 0 || xIn.compareTo(minInput) < 0 || xIn.compareTo(M) == 0) {
                                throw new IllegalArgumentException();
                            }
                            n = newtonMethod(xIn);
                            out.println("Converged after " + n + " steps");
                        } catch (NumberFormatException e) {
                            out.println("Unable to format input");
                        } catch (ArithmeticException e) {
                            out.println("Unable to process input");
                        } catch (IllegalArgumentException e) {
                            out.println("Invalid Input");
                        }
                    } else {
                        out.println("Invalid input");
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
