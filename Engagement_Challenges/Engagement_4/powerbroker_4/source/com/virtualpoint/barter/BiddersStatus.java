package com.virtualpoint.barter;

import com.virtualpoint.talkers.DialogsPublicIdentity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


/**
 * Data structure to hold all the information a seller needs to pick a winner:
 * Valid claims of winners, concessions of losers, and list of bidders who haven't yet submitted either
 */
public class BiddersStatus {
	private Map<DialogsPublicIdentity, Integer> claimedWinners; // users who have presented a valid claim as winner of the auction
	private Set<DialogsPublicIdentity> conceders; // users who have conceded this auction (informed us that they didn't win)
												  // Note: currently, only the seller receives concessions
	private Set<DialogsPublicIdentity> biddersNotReported; // users who bid on this auction but haven't yet provided a concession or valid win claim
	
	public BiddersStatus(){
		claimedWinners = new HashMap<DialogsPublicIdentity, Integer>();
		conceders = new TreeSet<DialogsPublicIdentity>();
		biddersNotReported = new TreeSet<DialogsPublicIdentity>();
	}
	
	public boolean verifyHighest(int claimedBid){
		int highest=0;
		for (DialogsPublicIdentity user: claimedWinners.keySet()){
			int bid = claimedWinners.get(user);
			if (bid>highest){
				highest = bid;
			}
		}
		return claimedBid==highest;
	}
	
	public void addBidder(DialogsPublicIdentity user){
		biddersNotReported.add(user);
	}
	
	// remove bidder from biddersNotReported
	public void removeBidder(DialogsPublicIdentity user){
		biddersNotReported.remove(user); 
	}
	
	public void addConcession(DialogsPublicIdentity user){
		conceders.add(user);
		removeBidder(user);
	}
	
	public void addWinClaim(DialogsPublicIdentity user, int bid){
		claimedWinners.put(user, bid);
		removeBidder(user);
	}
	
	public String toString(){
		String result ="";
		result += "Bidders who claim to have won:\n";
		for (DialogsPublicIdentity user : new TreeSet<>(claimedWinners.keySet())){ // treeset to make sure output is consistently ordered
			result += user.obtainId() + ", with bid of $" + claimedWinners.get(user) + "\n";
		}
		result += "Bidders who have conceded:\n";
		for (DialogsPublicIdentity user: conceders){
			result += user.obtainId() + "\n";
		}
		result += "Remaining bidders:\n";
		for (DialogsPublicIdentity user: biddersNotReported){
			result += user.obtainId() + "\n";
		}
		return result;
	}
}

