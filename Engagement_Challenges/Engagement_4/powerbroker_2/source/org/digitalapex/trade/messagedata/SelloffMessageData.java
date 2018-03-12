package org.digitalapex.trade.messagedata;

/**
 * Class to represent all the types of messages with so little data that it's not worth making their own class.
 * Also parent class for all BidPalMessageData classes 
 */
public abstract class SelloffMessageData {
	public enum MessageType {AUCTION_START, BID_RECEIPT, BID_COMMITMENT, BID_COMPARISON, BIDDING_OVER, CLAIM_WIN, CONCESSION, AUCTION_END};
	
	protected static SelloffSerializer serializer;
	public final MessageType type;
	
	public String selloffId;
	
	public SelloffMessageData(MessageType type, String selloffId){
		this.type = type;
		this.selloffId = selloffId;
	}
	
	public static void setSerializer(SelloffSerializer serialer){
		serializer = serialer;
	}
	
	public String fetchSelloffId(){
		return selloffId;
	}
	
	/////////////////////////  simple subclasses are defined below /////////////////////////
	
	
	public static class SelloffStart extends SelloffMessageData {
		public String description;
		public SelloffStart(String id, String desc){
			super(MessageType.AUCTION_START, id);
			description = desc;
		}
	}

	public static class BidReceipt extends SelloffMessageData {
		public BidReceipt(String id){super(MessageType.BID_RECEIPT, id);}
	}

	public static class BiddingOver extends SelloffMessageData {
		public BiddingOver(String id){
			super(MessageType.BIDDING_OVER, id);
		}
	}
	
	public static class Concession extends SelloffMessageData {
		public Concession(String id){
			super(MessageType.CONCESSION, id);
		}
	}
	
	public static class SelloffEnd extends SelloffMessageData {
		public String winner;
		public int winningBid;
		public SelloffEnd(String id, String winner, int winningBid){
			super(MessageType.AUCTION_END, id);
			this.winner = winner;
			this.winningBid = winningBid;
		}
	}
}

