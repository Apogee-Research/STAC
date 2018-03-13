package infotraderproof.sometests;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author user
 */
public class MakeHTTPRequest {

    public static void main(String[] args) {

        try {
            String l = "begin";
            Files.write(Paths.get("logs/order"), l.getBytes(), StandardOpenOption.DELETE_ON_CLOSE);
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
        try {
            String l = "begin\n";
            Files.write(Paths.get("logs/order"), l.getBytes(), StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }

        List<String> mlist = getMList("security");
        System.out.println(mlist);

        List<String> to = new ArrayList();
        to.add("security");
        to.add("security");
        to.add("abc");
        sendEmail("me", to, "subj", "the body");
    }

    public static List<String> getMList(String mlist) {

        String excutePost = excutePost("http://127.0.0.1:8988/address.cgi", ("list=" + mlist+"@smartmail.com"));

        System.out.println("public addresses in list (" + mlist + "):" + excutePost);
        String[] split = excutePost.split(";");

        //List<String> asList = Arrays.asList(split);
        List<String> res = new ArrayList();
        for (int i = 0; i < split.length; i++) {
            if (split[i].contains("@")) {
                res.add(split[i]);
            }
        }

        return res;

    }

    public static String sendEmail(String from, List<String> to, String subj, String body) {

        StringBuilder tos = new StringBuilder();
        Iterator<String> it = to.iterator();
        while (it.hasNext()) {
            tos.append(it.next() + "@smartmail.com;");
        }

        String excutePost = excutePost("http://127.0.0.1:8988/email.cgi", ("from=" + from+"@smartmail.com") + "&" + ("to=" + tos) + "&" + ("subj=" + subj) + "&" + ("content=" + body));

        return excutePost;

    }

    public static String excutePost(String targetURL, String urlParameters) {
        HttpURLConnection connection = null;
        try {
            //Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length",
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.close();

            //Get Response  
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if not Java 5+ 
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}
