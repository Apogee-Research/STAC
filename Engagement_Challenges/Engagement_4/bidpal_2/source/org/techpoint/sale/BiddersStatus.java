package org.techpoint.sale;

import org.techpoint.communications.CommsPublicIdentity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


/**
 * Data structure to hold all the information a seller needs to pick a winner:
 * Valid claims of winners, concessions of losers, and list of bidders who haven't yet submitted either
 */
public class BiddersStatus {
	private Map<CommsPublicIdentity, Integer> claimedWinners; // users who have presented a valid claim as winner of the auction
	private Set<CommsPublicIdentity> conceders; // users who have conceded this auction (informed us that they didn't win)
												  // Note: currently, only the seller receives concessions
	private Set<CommsPublicIdentity> biddersNotReported; // users who bid on this auction but haven't yet provided a concession or valid win claim
	
	public BiddersStatus(){
		claimedWinners = new HashMap<CommsPublicIdentity, Integer>();
		conceders = new TreeSet<CommsPublicIdentity>();
		biddersNotReported = new TreeSet<CommsPublicIdentity>();
	}
	
	public boolean verifyHighest(int claimedProposal){
		int highest=0;
		for (CommsPublicIdentity member : claimedWinners.keySet()){
			int proposal = claimedWinners.get(member);
			if (proposal >highest){
				highest = proposal;
			}
		}
		return claimedProposal ==highest;
	}
	
	public void addBidder(CommsPublicIdentity member){
		biddersNotReported.add(member);
	}
	
	// remove bidder from biddersNotReported
	public void removeBidder(CommsPublicIdentity member){
		biddersNotReported.remove(member);
	}
	
	public void addConcession(CommsPublicIdentity member){
		conceders.add(member);
		removeBidder(member);
	}
	
	public void addWinClaim(CommsPublicIdentity member, int proposal){
		claimedWinners.put(member, proposal);
		removeBidder(member);
	}
	
	public String toString(){
		String result ="";
		result += "Bidders who claim to have won:\n";
		for (CommsPublicIdentity member : new TreeSet<>(claimedWinners.keySet())){ // treeset to make sure output is consistently ordered
			result += member.grabId() + ", with bid of $" + claimedWinners.get(member) + "\n";
		}
		result += "Bidders who have conceded:\n";
		for (CommsPublicIdentity member : conceders){
			result += member.grabId() + "\n";
		}
		result += "Remaining bidders:\n";
		for (CommsPublicIdentity member : biddersNotReported){
			result += member.grabId() + "\n";
		}
		return result;
	}
}

