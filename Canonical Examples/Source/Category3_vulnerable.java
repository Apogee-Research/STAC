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

//Only Best & Worst Case Bounds Considered
public class Category3_vulnerable {
    private static final int port = 8000;
    private static final int secret = 1234;
    private static final int n = 20;
    private static ServerSocket server;

    private static void checkSecret(int guess, int t) throws InterruptedException {
        if (guess <= secret) {
            if (t == 1) {
                Thread.sleep(1);
            } else if (t == 2) {
                for (int i = 0; i < n; i++) {
                    Thread.sleep(1);
                }
            } else {
                for (int i = 0; i < n * n * n; i++) {
                    Thread.sleep(1);
                }
            }
        } else {
            if (t == 1) {
                Thread.sleep(1);
            } else if (t == 2) {
                for (int i = 0; i < n * n; i++) {
                    Thread.sleep(1);
                }
            } else {
                for (int i = 0; i < n * n * n; i++) {
                    Thread.sleep(1);
                }
            }
        }
    }

    private static void startServer() {
        try {
            server = new ServerSocket(port);
            System.out.println("Server Started Port: " + port);
            Socket client;
            PrintWriter out;
            BufferedReader in;
            String userInput;
            int guess;
            int t;
            while (true) {
                client = server.accept();
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                userInput = in.readLine();
                try {
                    String[] userInputArgs = userInput.split("\\s+");
                    if (userInputArgs.length != 2) {
                        throw new IllegalArgumentException();
                    }
                    guess = Integer.parseInt(userInputArgs[0]);
                    if(guess < 0) {
                        throw new IllegalArgumentException();
                    }
                    t = Integer.parseInt(userInputArgs[1]);
                    checkSecret(guess, t);
                    out.println("Process Complete");
                } catch (IllegalArgumentException | InterruptedException e) {
                    out.println("Unable to Process Input");
                }
                client.shutdownOutput();
                client.shutdownInput();
                client.close();
            }
        } catch (IOException e) {
            System.exit(-1);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        startServer();
    }
}
