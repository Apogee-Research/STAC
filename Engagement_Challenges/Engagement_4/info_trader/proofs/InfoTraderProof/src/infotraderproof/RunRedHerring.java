/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package infotraderproof;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import static infotraderproof.sometests.POSTGETLegitFile.doPost;

/**
 *
 * @author user
 */
public class RunRedHerring {

    public static void main(String[] args) throws IOException {
        String readFile = null;
        readFile = readFile("redherring/Microsoft+Gains");
        doPost(readFile);
        System.out.println("--RED HERRING STATUS: everything should look normal now, no errors");
        readFile = null;
        readFile = readFile("redherring/New+eyePod+Released");
        doPost(readFile);
        System.out.println("--RED HERRING STATUS: should get an error message resulting from a stack overflow on server after adding this doc");

        
        System.out.println("--RED HERRING STATUS: expecting another stack overflow error when attempting to retrieve either of these docs -- this one should"
                + " take longer since we keep loading both docs in a loop until the overflow occurs");

        String excutePost = excutePost("http://127.0.0.1:8988/gdoc.cgi", ("name=Microsoft Gains&getAll=true"));

        System.out.println(excutePost);
        
        System.out.println("\n--RED HERRING STATUS: THIS SHOULD COMPLETE BELOW THE TIME COMPLEXITY ATTACK BUDGET FOR OUR REDHERRING -- WHICH WAS RECOMMENED FOR 2 MINUTES (MAY HAVE CHANGED, SEE DESIGN DOC FOR FINAL NUMBER)");
        System.out.println("\n--OUR TESTS SHOWED THIS HAPPENING IN LESS THAN 30 SECONDS. WE RECOMMEND TESTING HERE TO VERIFY -- IF YOU FIND IT TAKES LONGER WE HAVE A SIMPLE FIX THAT SHOULD MAKE STACKOVERFLOW ERROR HAPPEN VERY QUICKLY");
        
        
//excutePost = excutePost("http://127.0.0.1:8988/gdoc.cgi", ("name=New eyePod Released&getAll=true"));

        //System.out.println(excutePost);
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

    public static void doPost(String rawData) throws MalformedURLException, IOException {
        {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String reqdate = sdf.format(cal.getTime());
            System.out.println(reqdate);
        }
        //String rawData = "id=10";
        /*String type = "application/x-www-form-urlencoded";
         String encodedData = URLEncoder.encode( rawData ); 
         URL u = new URL("http://127.0.0.1:8988/doc.cgi");
         HttpURLConnection conn = (HttpURLConnection) u.openConnection();
         conn.setDoOutput(true);
         conn.setRequestMethod("POST");
         conn.setRequestProperty( "Content-Type", type );
         conn.setRequestProperty( "Content-Length", String.valueOf(encodedData.length()));
         OutputStream os = conn.getOutputStream();
         os.write(encodedData.getBytes());*/
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
        {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String reqdate = sdf.format(cal.getTime());
            System.out.println(reqdate);
        }
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
