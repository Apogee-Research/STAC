package com.virtualpoint.barter.messagedata;

/**
 * Class to represent all the types of messages with so little data that it's not worth making their own class.
 * Also parent class for all BidPalMessageData classes 
 */
public abstract class BarterMessageData {
	public enum MessageType {AUCTION_START, BID_RECEIPT, BID_COMMITMENT, BID_COMPARISON, BIDDING_OVER, CLAIM_WIN, CONCESSION, AUCTION_END};
	
	protected static BarterSerializer serializer;
	public final MessageType type;
	
	public String barterId;
	
	public BarterMessageData(MessageType type, String barterId){
		this.type = type;
		this.barterId = barterId;
	}
	
	public static void defineSerializer(BarterSerializer serialer){
		serializer = serialer;
	}
	
	public String obtainBarterId(){
		return barterId;
	}
	
	/////////////////////////  simple subclasses are defined below /////////////////////////
	
	
	public static class BarterStart extends BarterMessageData {
		public String description;
		public BarterStart(String id, String desc){
			super(MessageType.AUCTION_START, id);
			description = desc;
		}
	}

	public static class BidReceipt extends BarterMessageData {
		public BidReceipt(String id){super(MessageType.BID_RECEIPT, id);}
	}

	public static class BiddingOver extends BarterMessageData {
		public BiddingOver(String id){
			super(MessageType.BIDDING_OVER, id);
		}
	}
	
	public static class Concession extends BarterMessageData {
		public Concession(String id){
			super(MessageType.CONCESSION, id);
		}
	}
	
	public static class BarterEnd extends BarterMessageData {
		public String winner;
		public int winningBid;
		public BarterEnd(String id, String winner, int winningBid){
			super(MessageType.AUCTION_END, id);
			this.winner = winner;
			this.winningBid = winningBid;
		}
	}
}

