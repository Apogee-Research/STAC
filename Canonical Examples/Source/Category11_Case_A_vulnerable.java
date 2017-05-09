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
import java.util.Random;

//Obvious decoupling of (guess, secret) and boolean[] t
public class Category11_Case_A_vulnerable {
    private static final int secret = 1234; //secret
    private static final int port = 8000;
    private static ServerSocket server;
    private static Random random = new Random(79897); // Fixed random seed for purpose of example
    private static boolean[] t = new boolean[100];

    private static boolean checkSecret(int guess, boolean[] t) throws InterruptedException {
        recur(guess, t, t.length - 1);
        if (guess <= secret) {
            Thread.sleep(10);
        }
        return guess == secret;
    }

    private static void recur(int guess, boolean[] t, int index) {
        if (index == 0 && t[index]) {
        } else if (t[index]) {
            recur(guess, t, index - 1);
        }
    }

    private static void startServer() throws InterruptedException {
        try {
            System.out.println("Server Started port: " + port);
            server = new ServerSocket(port);
            Socket client;
            PrintWriter out;
            BufferedReader in;
            String userInput;
            int guess;
            boolean correct;
            randomizeT();

            while (true) {
                client = server.accept();
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                userInput = in.readLine();
                try {
                    guess = Integer.parseInt(userInput);
                    if(guess < 0) {
                        throw new IllegalArgumentException();
                    }
                    correct = checkSecret(guess, t);
                    if (correct) {
                        out.println("Correct");
                    } else {
                        out.println("Incorrect");
                    }
                } catch (IllegalArgumentException e) {
                    out.println("Unable to Process Request");
                }
                client.shutdownOutput();
                client.shutdownInput();
                client.close();
            }
        } catch (IOException e) {
            System.exit(-1);
        }
    }

    private static void randomizeT() {
        for (int x = 0; x < t.length; x++) {
            t[x] = random.nextBoolean();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        startServer();
    }
}