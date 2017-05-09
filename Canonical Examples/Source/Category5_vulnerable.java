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

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Headers;

//Input Budgets are Disregarded
public class Category5_vulnerable {
    private static HashMap<String, String[]> sessionMap;
    private static HttpServer server;
    private static Random random;

    private static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/Category5", new Category5Handler());
        server.setExecutor(null);
        server.start();
    }

    private static void stopServer() {
        System.out.println("Server Started on 8000\nType q to stop");
        Scanner sc = new Scanner(System.in);
        while (true) {
            if (sc.nextLine().equals("q")) {
                server.stop(0);
                System.out.println("Server Stopped");
                System.exit(0);
            }
        }
    }

    public static void main(String[] args) {
        sessionMap = new HashMap<>();
        random = new Random();
        try {
            startServer();
            stopServer();
        } catch (Exception e) {
            server.stop(0);
            e.printStackTrace();
        }
    }

    private static class Category5Handler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            //Get Session Info
            String response;
            String session = getSessionID(t.getRequestHeaders());

            //Did user submit a valid sessionID if so resume session
            if (session != null && validSession(session)) {
                response = resumeSession(session);
            }
            //Otherwise start a new session
            else if (session != null) {
                response = startNewSession(t, "Unable to Resume Session\n");
            } else {
                response = startNewSession(t, "");
            }

            sendResponse(t, response);
        }

        private String getSessionID(Headers requestH) {
            List<String> requestTest = requestH.get("Cookie");
            if (requestTest == null) {
                return null;
            }
            return requestTest.get(0);
        }

        private boolean validSession(String session) {
            return sessionMap.keySet().contains(session);
        }

        private String resumeSession(String session) {
            sessionMap.get(session);
            return "Resumed Session\n";
        }

        private String startNewSession(HttpExchange t, String response) {
            //Start New Session
            String[] userSetup = new String[10000000];
            Arrays.fill(userSetup, "setup");
            String session = createSessionID();
            sessionMap.put(session, userSetup);

            //Setting Session Cookie
            Headers responseH = t.getResponseHeaders();
            responseH.add("Set-Cookie", session);

            return response + "Started New Session\n";
        }

        private String createSessionID() {
            String randomCookie = String.format("%020d", Math.abs(random.nextInt()));
            while (sessionMap.keySet().contains(randomCookie)) {
                randomCookie = String.format("%020d", Math.abs(random.nextInt()));
            }
            return randomCookie;
        }

        private void sendResponse(HttpExchange t, String response) throws IOException {
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
