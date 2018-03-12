package org.digitalapex.trade;

import org.digitalapex.talkers.TalkersPublicIdentity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


/**
 * Data structure to hold all the information a seller needs to pick a winner:
 * Valid claims of winners, concessions of losers, and list of bidders who haven't yet submitted either
 */
public class BiddersStatus {
	private Map<TalkersPublicIdentity, Integer> claimedWinners; // users who have presented a valid claim as winner of the auction
	private Set<TalkersPublicIdentity> conceders; // users who have conceded this auction (informed us that they didn't win)
												  // Note: currently, only the seller receives concessions
	private Set<TalkersPublicIdentity> biddersNotReported; // users who bid on this auction but haven't yet provided a concession or valid win claim
	
	public BiddersStatus(){
		claimedWinners = new HashMap<TalkersPublicIdentity, Integer>();
		conceders = new TreeSet<TalkersPublicIdentity>();
		biddersNotReported = new TreeSet<TalkersPublicIdentity>();
	}
	
	public boolean verifyHighest(int claimedBid){
		int highest=0;
		for (TalkersPublicIdentity user: claimedWinners.keySet()){
			int bid = claimedWinners.get(user);
			if (bid>highest){
				highest = bid;
			}
		}
		return claimedBid==highest;
	}
	
	public void addBidder(TalkersPublicIdentity user){
		biddersNotReported.add(user);
	}
	
	// remove bidder from biddersNotReported
	public void removeBidder(TalkersPublicIdentity user){
		biddersNotReported.remove(user); 
	}
	
	public void addConcession(TalkersPublicIdentity user){
		conceders.add(user);
		removeBidder(user);
	}
	
	public void addWinClaim(TalkersPublicIdentity user, int bid){
		claimedWinners.put(user, bid);
		removeBidder(user);
	}
	
	public String toString(){
		String result ="";
		result += "Bidders who claim to have won:\n";
		for (TalkersPublicIdentity user : new TreeSet<>(claimedWinners.keySet())){ // treeset to make sure output is consistently ordered
			result += user.getId() + ", with bid of $" + claimedWinners.get(user) + "\n";
		}
		result += "Bidders who have conceded:\n";
		for (TalkersPublicIdentity user: conceders){
			result += user.getId() + "\n";
		}
		result += "Remaining bidders:\n";
		for (TalkersPublicIdentity user: biddersNotReported){
			result += user.getId() + "\n";
		}
		return result;
	}
}

