package net.roboticapex.selloff.messagedata;

/**
 * Class to represent all the types of messages with so little data that it's not worth making their own class.
 * Also parent class for all BidPalMessageData classes 
 */
public abstract class TradeMessageData {
	public enum MessageType {AUCTION_START, BID_RECEIPT, BID_COMMITMENT, BID_COMPARISON, BIDDING_OVER, CLAIM_WIN, CONCESSION, AUCTION_END};
	
	protected static TradeSerializer serializer;
	public final MessageType type;
	
	public String tradeId;
	
	public TradeMessageData(MessageType type, String tradeId){
		this.type = type;
		this.tradeId = tradeId;
	}
	
	public static void fixSerializer(TradeSerializer serialer){
		serializer = serialer;
	}
	
	public String obtainTradeId(){
		return tradeId;
	}
	
	/////////////////////////  simple subclasses are defined below /////////////////////////
	
	
	public static class TradeStart extends TradeMessageData {
		public String description;
		public TradeStart(String id, String desc){
			super(MessageType.AUCTION_START, id);
			description = desc;
		}
	}

	public static class PromiseReceipt extends TradeMessageData {
		public PromiseReceipt(String id){super(MessageType.BID_RECEIPT, id);}
	}

	public static class BiddingOver extends TradeMessageData {
		public BiddingOver(String id){
			super(MessageType.BIDDING_OVER, id);
		}
	}
	
	public static class Concession extends TradeMessageData {
		public Concession(String id){
			super(MessageType.CONCESSION, id);
		}
	}
	
	public static class TradeEnd extends TradeMessageData {
		public String winner;
		public int winningPromise;
		public TradeEnd(String id, String winner, int winningPromise){
			super(MessageType.AUCTION_END, id);
			this.winner = winner;
			this.winningPromise = winningPromise;
		}
	}
}

