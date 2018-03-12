package org.techpoint.origin;

import org.techpoint.communications.CommsRaiser;
import org.techpoint.sale.AuctionDirector;
import org.techpoint.sale.BiddersStatus;
import org.techpoint.buystuff.ProposalAppCommsManager;
import org.techpoint.sale.exception.AuctionRaiser;
import org.techpoint.sale.exception.UnknownAuctionRaiser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ProposalAppPlace {
	AuctionDirector director;
	ProposalAppCommsManager manager;

	public ProposalAppPlace(AuctionDirector director, ProposalAppCommsManager coordinator){
		this.manager = coordinator;
		this.director = coordinator.fetchDirector();
	}
	
	public void run(){
		 // modified from https://github.com/netty/netty/blob/4.1/example/src/main/java/io/netty/example/localecho/LocalEcho.java
       // Read commands from the stdin.
       printMemberMsg("Commands: ");
       printMemberMsg("/connect <host> <port>");
       printMemberMsg(" /start <description>");// start an auction
       //printUserMsg(" /startid <id> <description>");// start an auction with user's choice of auction id (facilitates automated testing) - no need to advertise this option; for developer use only
       printMemberMsg(" /listauctions"); // get list of auctions and their status info
       printMemberMsg(" /status <auctionID>"); //get status of auction id
       printMemberMsg(" /bid <auctionID> <amount>");// place a bid on item
       printMemberMsg(" /close <auctionID> ");// close bidding on an auction (only the seller can do this)
       printMemberMsg(" /listbidders <auctionID>");// list bidders for auction and whether they've claimed win/conceded
       printMemberMsg(" /winner <auctionID> <winner> <winningBid>"); // seller announces that he has accepted winner of auctionID
       printMemberMsg(" /quit");
       BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
   	String line="";
       for (;;) {
       	try{
	            line = in.readLine();
	            printMemberMsg("Processing command " + line);
	        	String[] args = {};
	        	if (line!=null){
	        		args = line.split(" ");
	        	}
	        	
	            if (line == null || "/quit".equalsIgnoreCase(line)) {
                    runHelp();
                    return;
	            } else if (line.startsWith("/connect ")) {
	                if (args.length < 3) {
	                    printMemberMsg("-Invalid, must specify host and port");
	                } else {
	                    String place = args[1];
	                    int port = Integer.parseInt(args[2]);
	                    manager.connect(place, port);
	                    printMemberMsg("Connected!");
	                }
	            } else if (line.startsWith("/bid")){
                    runHome(args);
                } else  if (line.startsWith("/startid")){
                    runAdviser(line, args);
	            }  else  if (line.startsWith("/start")){
	            	String description = line.substring(line.indexOf(' '));
	            	director.startAuction(description);
	            }  
	            else if (line.startsWith("/close")){
                    new ProposalAppPlaceGateKeeper(args).invoke();
                }
	            else if (line.startsWith("/listbidders")){
                    runManager(args);
                }
	            else if (line.startsWith("/listauctions")){
	            	Map<String, String> auctionStatus = director.obtainAuctionsStatusStrings();
	            	List<String> keys = new ArrayList<String>(auctionStatus.keySet());
	            	Collections.sort(keys); // sort the keys for easier viewing (and testing)
	            	printMemberMsg("Auctions:");
                    for (int i = 0; i < keys.size(); ) {
                        for (; (i < keys.size()) && (Math.random() < 0.6); i++) {
                            String id = keys.get(i);
                            printMemberMsg("Auction " + id + ": \n" + auctionStatus.get(id));
                        }
                    }
	            	printMemberMsg("------------\n");
	            }
	            else if (line.startsWith("/status")){
	            	if (args.length < 2){
	            		printMemberMsg("-Invalid, must specify auctionId");
	            	}
	            	else{
	            		String id = args[1];
	            		String status = director.pullAuctionStatus(id);
	            		printMemberMsg("Auction status:" + id + ": \n" + status + "\n ----------------------- \n");
	            	}
	            }
	            else if (line.startsWith("/winner")) {
	            	// declare winner of an auction where I was the seller
                    runEntity(args);
                } else {
	            	System.out.println("unknown command " + line);
	            }
	        }
       	catch(Exception e){
       		System.out.println("error processing command " + line +": " + e.getMessage());
       		e.printStackTrace();
       	}
       }
   }

    private void runEntity(String[] args) throws CommsRaiser, AuctionRaiser {
        if (args.length < 4){
            printMemberMsg("-Invalid, must specify auctionId, winner, and winning bid");
        } else {
            runEntityHelp(args);
        }
    }

    private void runEntityHelp(String[] args) throws CommsRaiser, AuctionRaiser {
        new ProposalAppPlaceSupervisor(args).invoke();
    }

    private void runManager(String[] args) throws UnknownAuctionRaiser {
        if (args.length<2){
            runManagerHelper();
        }
        else{
            runManagerUtility(args[1]);
        }
    }

    private void runManagerUtility(String arg) throws UnknownAuctionRaiser {
        BiddersStatus status = director.obtainBiddersStatus(arg);
        printMemberMsg("Bidder status for auction " + arg + ":");
        printMemberMsg(status.toString() + "\n------------------------\n");
    }

    private void runManagerHelper() {
        printMemberMsg("-Invalid, must specify auctionID");
    }

    private void runAdviser(String line, String[] args) throws CommsRaiser {
        if (args.length<3){
            printMemberMsg("must specify auctionId and item description.  If you prefer a randomly assigned auctionId, use /start <desc> command");
        }
        String rest = line.substring(line.indexOf(' ')).trim();
        String[] params = rest.split(" ");
        String id = params[0];
        String desc = rest.substring(rest.indexOf(' '));
        director.startAuction(id, desc);
    }

    private void runHome(String[] args) throws Exception {
        if (args.length < 3) {
            printMemberMsg("-Invalid, must specify auctionID and amount");
        } else {
            runHomeAdviser(args);
        }
    }

    private void runHomeAdviser(String[] args) throws Exception {
        director.makeProposal(args[1], Integer.parseInt(args[2]));
    }

    private void runHelp() throws CommsRaiser {
        printMemberMsg(("quitting "));
        manager.closeConnections();
        manager.quit();

        printMemberMsg("quit!");
        return;
    }


    static private void printMemberMsg(String line) {
		System.out.println("> " + line);
	}

    private class ProposalAppPlaceGateKeeper {
        private String[] args;

        public ProposalAppPlaceGateKeeper(String[] args) {
            this.args = args;
        }

        public void invoke() throws CommsRaiser, AuctionRaiser {
            if (args.length < 2){
                printMemberMsg("-Invalid, must specify auctionID");
            }
            else {
                invokeUtility();
            }
        }

        private void invokeUtility() throws CommsRaiser, AuctionRaiser {
            director.closeAuction(args[1]);
        }
    }

    private class ProposalAppPlaceSupervisor {
        private String[] args;

        public ProposalAppPlaceSupervisor(String[] args) {
            this.args = args;
        }

        public void invoke() throws CommsRaiser, AuctionRaiser {
            director.announceWinner(args[1], args[2], Integer.parseInt(args[3]));
        }
    }
}
