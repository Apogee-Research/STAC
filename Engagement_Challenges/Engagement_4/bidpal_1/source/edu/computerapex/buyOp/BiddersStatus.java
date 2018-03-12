package edu.computerapex.buyOp;

import edu.computerapex.dialogs.CommunicationsPublicIdentity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


/**
 * Data structure to hold all the information a seller needs to pick a winner:
 * Valid claims of winners, concessions of losers, and list of bidders who haven't yet submitted either
 */
public class BiddersStatus {
	private Map<CommunicationsPublicIdentity, Integer> claimedWinners; // users who have presented a valid claim as winner of the auction
	private Set<CommunicationsPublicIdentity> conceders; // users who have conceded this auction (informed us that they didn't win)
												  // Note: currently, only the seller receives concessions
	private Set<CommunicationsPublicIdentity> biddersNotReported; // users who bid on this auction but haven't yet provided a concession or valid win claim
	
	public BiddersStatus(){
		claimedWinners = new HashMap<CommunicationsPublicIdentity, Integer>();
		conceders = new TreeSet<CommunicationsPublicIdentity>();
		biddersNotReported = new TreeSet<CommunicationsPublicIdentity>();
	}
	
	public boolean verifyHighest(int claimedBid){
		int highest=0;
		for (CommunicationsPublicIdentity participant : claimedWinners.keySet()){
			int bid = claimedWinners.get(participant);
			if (bid>highest){
				highest = bid;
			}
		}
		return claimedBid==highest;
	}
	
	public void addBidder(CommunicationsPublicIdentity participant){
		biddersNotReported.add(participant);
	}
	
	// remove bidder from biddersNotReported
	public void removeBidder(CommunicationsPublicIdentity participant){
		biddersNotReported.remove(participant);
	}
	
	public void addConcession(CommunicationsPublicIdentity participant){
		conceders.add(participant);
		removeBidder(participant);
	}
	
	public void addWinClaim(CommunicationsPublicIdentity participant, int bid){
		claimedWinners.put(participant, bid);
		removeBidder(participant);
	}
	
	public String toString(){
		String result ="";
		result += "Bidders who claim to have won:\n";
		for (CommunicationsPublicIdentity participant : new TreeSet<>(claimedWinners.keySet())){ // treeset to make sure output is consistently ordered
			result += participant.takeId() + ", with bid of $" + claimedWinners.get(participant) + "\n";
		}
		result += "Bidders who have conceded:\n";
		for (CommunicationsPublicIdentity participant : conceders){
			result += participant.takeId() + "\n";
		}
		result += "Remaining bidders:\n";
		for (CommunicationsPublicIdentity participant : biddersNotReported){
			result += participant.takeId() + "\n";
		}
		return result;
	}
}

