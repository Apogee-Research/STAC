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

//SC without conditional branches (use of try catch instead)
//Not vulnerable due to balanced delay() and delay()2 calls
public class Category12_not_vulnerable_exception {
    private static final int port = 8000;
    private static final String password = "zzzzzzzzzz";
    private static final int maxInputLen = 10;
    private static ServerSocket server;
    private static int subsequentCorrect;
    private static int exceedPasswordLen;

    private static void delay() {
        long start = System.nanoTime();
        while (System.nanoTime() - start < 1140000) {
        }
    }

    private static void delay2() {
        long start = System.nanoTime();
        while (System.nanoTime() - start < 80000) {
        }
    }

    private static void checkChar(String candidate, int charNumber) {
        try {
            int checkEqual = 100 / (password.charAt(charNumber - 1) - candidate.charAt(charNumber - 1));
            delay();
            delay2();
        } catch (ArithmeticException e1) {
            try {
                int checkDelay = 100 / (subsequentCorrect + 1 - charNumber);
                delay();
            } catch (ArithmeticException e2) {
                subsequentCorrect++;
                delay();
            }
        } catch (StringIndexOutOfBoundsException e3) {
            exceedPasswordLen++;
            delay();
        }
    }

    private static boolean verifyCredentials(String candidate) {
        subsequentCorrect = exceedPasswordLen = 0;
        for (int x = 0; x < candidate.length(); x++) {
            checkChar(candidate, x + 1);
        }
        return subsequentCorrect == password.length() && exceedPasswordLen == 0;
    }

    private static void startServer() {
        try {
            System.out.println("Server Started port: " + port);
            server = new ServerSocket(port);
            Socket client;
            PrintWriter out;
            BufferedReader in;
            String userInput;
            boolean correct;
            while (true) {
                client = server.accept();
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                userInput = in.readLine();
                if (userInput.length() <= maxInputLen && userInput.matches("[a-z]+")) {
                    correct = verifyCredentials(userInput);
                    delay();
                    if (correct) {
                        out.println("Correct");
                    } else {
                        out.println("Incorrect");
                    }
                } else {
                    out.println("Invalid Input");
                }
                client.shutdownOutput();
                client.shutdownInput();
                client.close();
            }
        } catch (IOException e) {
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        startServer();
    }
}
