package edu.computerapex.origin;

import edu.computerapex.buyOp.BarterDriver;
import edu.computerapex.buyOp.BiddersStatus;
import edu.computerapex.buyOp.bad.BarterDeviation;
import edu.computerapex.buyOp.bad.UnknownBarterDeviation;
import edu.computerapex.buystuff.BidPalCommunicationsHandler;
import edu.computerapex.dialogs.CommunicationsDeviation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BidPalHost {
	BarterDriver driver;
	BidPalCommunicationsHandler handler;

	public BidPalHost(BarterDriver driver, BidPalCommunicationsHandler handler){
		this.handler = handler;
		this.driver = handler.pullDriver();
	}
	
	public void run(){
		 // modified from https://github.com/netty/netty/blob/4.1/example/src/main/java/io/netty/example/localecho/LocalEcho.java
       // Read commands from the stdin.
       printParticipantMsg("Commands: ");
       printParticipantMsg("/connect <host> <port>");
       printParticipantMsg(" /start <description>");// start an auction
       //printUserMsg(" /startid <id> <description>");// start an auction with user's choice of auction id (facilitates automated testing) - no need to advertise this option; for developer use only
       printParticipantMsg(" /listauctions"); // get list of auctions and their status info
       printParticipantMsg(" /status <auctionID>"); //get status of auction id
       printParticipantMsg(" /bid <auctionID> <amount>");// place a bid on item
       printParticipantMsg(" /close <auctionID> ");// close bidding on an auction (only the seller can do this)
       printParticipantMsg(" /listbidders <auctionID>");// list bidders for auction and whether they've claimed win/conceded
       printParticipantMsg(" /winner <auctionID> <winner> <winningBid>"); // seller announces that he has accepted winner of auctionID
       printParticipantMsg(" /quit");
       BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
   	String line="";
       for (;;) {
       	try{
	            line = in.readLine();
	            printParticipantMsg("Processing command " + line);
	        	String[] args = {};
	        	if (line!=null){
	        		args = line.split(" ");
	        	}
	        	
	            if (line == null || "/quit".equalsIgnoreCase(line)) {
	            	printParticipantMsg(("quitting "));
	            	handler.closeConnections();
	            	handler.quit();

	            	printParticipantMsg("quit!");
	            	return;
	            } else if (line.startsWith("/connect ")) {
	                if (args.length < 3) {
                        runExecutor();
                    } else {
	                    String host = args[1];
	                    int port = Integer.parseInt(args[2]);
	                    handler.connect(host, port);
	                    printParticipantMsg("Connected!");
	                }
	            } else if (line.startsWith("/bid")){
	                if (args.length < 3) {
                        runEntity();
                    } else {
                        runCoordinator(args);
                    }
	            } else  if (line.startsWith("/startid")){
	            	if (args.length<3){
                        runUtility();
                    }
	            	String rest = line.substring(line.indexOf(' ')).trim();
	            	String[] params = rest.split(" ");
	            	String id = params[0];
	            	String desc = rest.substring(rest.indexOf(' '));
	            	driver.startBarter(id, desc);
	            }  else  if (line.startsWith("/start")){
                    runWorker(line);
	            }  
	            else if (line.startsWith("/close")){
	            	if (args.length < 2){
                        runTarget();
                    }
	            	else {
                        runHelper(args[1]);
                    }
	            } 
	            else if (line.startsWith("/listbidders")){
	            	if (args.length<2){
	            		printParticipantMsg("-Invalid, must specify auctionID");
	            	}
	            	else{
                        runHome(args[1]);
	            	}
	            }
	            else if (line.startsWith("/listauctions")){
	            	Map<String, String> barterStatus = driver.obtainBartersStatusStrings();
	            	List<String> keys = new ArrayList<String>(barterStatus.keySet());
	            	Collections.sort(keys); // sort the keys for easier viewing (and testing)
	            	printParticipantMsg("Auctions:");
                    for (int c = 0; c < keys.size(); ) {
                        while ((c < keys.size()) && (Math.random() < 0.6)) {
                            while ((c < keys.size()) && (Math.random() < 0.6)) {
                                for (; (c < keys.size()) && (Math.random() < 0.6); c++) {
                                    String id = keys.get(c);
                                    printParticipantMsg("Auction " + id + ": \n" + barterStatus.get(id));
                                }
                            }
                        }
                    }
	            	printParticipantMsg("------------\n");
	            }
	            else if (line.startsWith("/status")){
	            	if (args.length < 2){
                        runAid();
                    }
	            	else{
	            		String id = args[1];
	            		String status = driver.grabBarterStatus(id);
	            		printParticipantMsg("Auction status:" + id + ": \n" + status + "\n ----------------------- \n");
	            	}
	            }
	            else if (line.startsWith("/winner")) {
	            	// declare winner of an auction where I was the seller
                    runHelp(args);
                } else {
                    runGateKeeper(line);
                }
	        }
       	catch(Exception e){
       		System.out.println("error processing command " + line +": " + e.getMessage());
       		e.printStackTrace();
       	}
       }
   }

    private void runGateKeeper(String line) {
        System.out.println("unknown command " + line);
    }

    private void runHelp(String[] args) throws CommunicationsDeviation, BarterDeviation {
        new BidPalHostAssist(args).invoke();
    }

    private void runAid() {
        printParticipantMsg("-Invalid, must specify auctionId");
    }

    private void runHome(String arg) throws UnknownBarterDeviation {
        BiddersStatus status = driver.getBiddersStatus(arg);
        printParticipantMsg("Bidder status for auction " + arg + ":");
        printParticipantMsg(status.toString() + "\n------------------------\n");
    }

    private void runHelper(String arg) throws CommunicationsDeviation, BarterDeviation {
        driver.closeBarter(arg);
    }

    private void runTarget() {
        printParticipantMsg("-Invalid, must specify auctionID");
    }

    private void runWorker(String line) throws CommunicationsDeviation {
        String description = line.substring(line.indexOf(' '));
        driver.startBarter(description);
    }

    private void runUtility() {
        printParticipantMsg("must specify auctionId and item description.  If you prefer a randomly assigned auctionId, use /start <desc> command");
    }

    private void runCoordinator(String[] args) throws Exception {
        driver.makeBid(args[1], Integer.parseInt(args[2]));
    }

    private void runEntity() {
        printParticipantMsg("-Invalid, must specify auctionID and amount");
    }

    private void runExecutor() {
        printParticipantMsg("-Invalid, must specify host and port");
    }


    static private void printParticipantMsg(String line) {
		System.out.println("> " + line);
	}

    private class BidPalHostAssist {
        private String[] args;

        public BidPalHostAssist(String[] args) {
            this.args = args;
        }

        public void invoke() throws CommunicationsDeviation, BarterDeviation {
            if (args.length < 4){
                printParticipantMsg("-Invalid, must specify auctionId, winner, and winning bid");
            } else {
                driver.announceWinner(args[1], args[2], Integer.parseInt(args[3]));
            }
        }
    }
}
