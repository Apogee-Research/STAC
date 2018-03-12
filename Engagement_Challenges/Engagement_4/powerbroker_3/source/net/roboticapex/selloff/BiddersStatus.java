package net.roboticapex.selloff;

import net.roboticapex.senderReceivers.SenderReceiversPublicIdentity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


/**
 * Data structure to hold all the information a seller needs to pick a winner:
 * Valid claims of winners, concessions of losers, and list of bidders who haven't yet submitted either
 */
public class BiddersStatus {
	private Map<SenderReceiversPublicIdentity, Integer> claimedWinners; // users who have presented a valid claim as winner of the auction
	private Set<SenderReceiversPublicIdentity> conceders; // users who have conceded this auction (informed us that they didn't win)
												  // Note: currently, only the seller receives concessions
	private Set<SenderReceiversPublicIdentity> biddersNotReported; // users who bid on this auction but haven't yet provided a concession or valid win claim
	
	public BiddersStatus(){
		claimedWinners = new HashMap<SenderReceiversPublicIdentity, Integer>();
		conceders = new TreeSet<SenderReceiversPublicIdentity>();
		biddersNotReported = new TreeSet<SenderReceiversPublicIdentity>();
	}
	
	public boolean verifyHighest(int claimedPromise){
		int highest=0;
		for (SenderReceiversPublicIdentity user: claimedWinners.keySet()){
			int promise = claimedWinners.get(user);
			if (promise >highest){
				highest = promise;
			}
		}
		return claimedPromise ==highest;
	}
	
	public void addBidder(SenderReceiversPublicIdentity user){
		biddersNotReported.add(user);
	}
	
	// remove bidder from biddersNotReported
	public void removeBidder(SenderReceiversPublicIdentity user){
		biddersNotReported.remove(user); 
	}
	
	public void addConcession(SenderReceiversPublicIdentity user){
		conceders.add(user);
		removeBidder(user);
	}
	
	public void addWinClaim(SenderReceiversPublicIdentity user, int promise){
		claimedWinners.put(user, promise);
		removeBidder(user);
	}
	
	public String toString(){
		String result ="";
		result += "Bidders who claim to have won:\n";
		for (SenderReceiversPublicIdentity user : new TreeSet<>(claimedWinners.keySet())){ // treeset to make sure output is consistently ordered
			result += user.obtainId() + ", with bid of $" + claimedWinners.get(user) + "\n";
		}
		result += "Bidders who have conceded:\n";
		for (SenderReceiversPublicIdentity user: conceders){
			result += user.obtainId() + "\n";
		}
		result += "Remaining bidders:\n";
		for (SenderReceiversPublicIdentity user: biddersNotReported){
			result += user.obtainId() + "\n";
		}
		return result;
	}
}

