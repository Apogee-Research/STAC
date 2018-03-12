package com.virtualpoint.barter.messagedata;

import com.virtualpoint.barter.AuctionProtos;
import com.virtualpoint.barter.AuctionProtos.AuctionEndMsg;
import com.virtualpoint.barter.AuctionProtos.AuctionMsg;
import com.virtualpoint.barter.AuctionProtos.AuctionMsg.Type;
import com.virtualpoint.barter.AuctionProtos.AuctionStartMsg;
import com.virtualpoint.barter.AuctionProtos.BidCommitmentMsg;
import com.virtualpoint.barter.AuctionProtos.BidComparisonMsg;
import com.virtualpoint.barter.AuctionProtos.BigIntegerMsg;
import com.virtualpoint.barter.AuctionProtos.RevealBidMsg;
import com.virtualpoint.barter.messagedata.BarterMessageData.BarterEnd;
import com.virtualpoint.barter.messagedata.BarterMessageData.BiddingOver;
import com.virtualpoint.barter.messagedata.BarterMessageData.Concession;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.math.BigInteger;


/**
 * Transforms AuctionMessageData to byte[] and back via the Google protocol buffers in AuctionProtos.
 */
public class BarterProtoSerializer extends BarterSerializer {
	public byte[] serialize(OfferSubmission data){
		byte[] bytes = null;

		BigIntegerMsg bint = BarterProtoSerializer.makeBigIntegerMsg(data.obtainSharedVal());
    	BidCommitmentMsg commit = 
    		AuctionProtos.BidCommitmentMsg.newBuilder()
    		.setHash(ByteString.copyFrom(data.grabHash()))
    		.setR1(data.fetchR1())
    		.setSharedVal(bint)
    		.build();
    	AuctionMsg msg = AuctionMsg.newBuilder()
    	    	.setAuctionId(data.obtainBarterId())
    			.setCommitment(commit)
    			.setType(AuctionMsg.Type.BID_COMMITMENT)
    			.build();
    	bytes =  msg.toByteArray();

    	return bytes;
	}
	
	public byte[] serialize(ExchangeData data){
		BigIntegerMsg pMsg = BarterProtoSerializer.makeBigIntegerMsg(data.getP());
		BidComparisonMsg.Builder compBuilder = BidComparisonMsg.newBuilder();
		compBuilder.setPrime(pMsg);
		compBuilder.setNeedReturnComparison(data.takeNeedReturn());
		
		for (int q =0; q < data.takeZLength(); q++){
			BigIntegerMsg vMsg = BarterProtoSerializer.makeBigIntegerMsg(data.obtainZ(q));
			compBuilder.addValues(vMsg); 
		}
		AuctionMsg msg = AuctionMsg.newBuilder()
			.setAuctionId(data.obtainBarterId())
			.setType(Type.BID_COMPARISON)
			.setComparison(compBuilder.build())
			.build();
		return msg.toByteArray();
		
	}
	
	public byte[] serialize(BidConveyData data){
		 RevealBidMsg claim = RevealBidMsg.newBuilder()
		 	.setX(BarterProtoSerializer.makeBigIntegerMsg(data.obtainX()))
		 	.setR1(data.fetchR1())
		 	.setR2(data.getR2())
		 	.setBid(data.takeBid())
		 	.build();
		 AuctionMsg msg =AuctionMsg.newBuilder()
		 .setAuctionId(data.obtainBarterId())
		 .setType(AuctionMsg.Type.CLAIM_WIN)
		 .setReveal(claim)
		 .build();
		 return msg.toByteArray();
	}
	
	public byte[] serialize(BarterMessageData.BarterStart barterStart){

    	 AuctionStartMsg startMsg = AuctionStartMsg.newBuilder()
		.setItemDescription(barterStart.description)
		.build();
	
    	 AuctionMsg msg = AuctionMsg.newBuilder()
	   		.setAuctionId(barterStart.obtainBarterId())
	   		.setType(AuctionMsg.Type.AUCTION_START)
	   		.setStart(startMsg)
	   		.build();
			return msg.toByteArray();
		
	}

	public byte[] serialize(BarterMessageData.BidReceipt bidReceipt){
		AuctionMsg msg = AuctionMsg.newBuilder()
				.setAuctionId(bidReceipt.obtainBarterId())
				.setType(AuctionMsg.Type.BID_RECEIPT)
				.build();
		return msg.toByteArray();
	}
	
	public byte[] serialize(BiddingOver overData){
		AuctionMsg msg = AuctionMsg.newBuilder()
		.setAuctionId(overData.obtainBarterId())
		.setType(AuctionMsg.Type.BIDDING_OVER)
		.build();
		return msg.toByteArray();
	}
	
	public byte[] serialize(Concession concession){
		AuctionMsg msg = AuctionMsg.newBuilder()
		.setAuctionId(concession.obtainBarterId())
		.setType(AuctionMsg.Type.CONCEDE)
		.build();
		return msg.toByteArray();
	}
	
	public  byte[] serialize(BarterEnd endData){

		AuctionEndMsg endMsg = AuctionEndMsg.newBuilder()	
    		.setWinner(endData.winner)
    		.setWinningBid(endData.winningBid)
    		.build();
		AuctionMsg msg = AuctionMsg.newBuilder()
    		.setAuctionId(endData.obtainBarterId())
    		.setType(AuctionMsg.Type.AUCTION_END)
    		.setEnd(endMsg)
    		.build();
    	return msg.toByteArray();

	}
	
	public BarterMessageData deserialize(byte[] bytes) throws IOException{
		try{
			AuctionMsg msg = AuctionMsg.parseFrom(bytes);
	    	String barterId = msg.getAuctionId();
	    	switch(msg.getType()){
	    	case AUCTION_START:
	    		AuctionStartMsg startMsg = msg.getStart();
	    		return new BarterMessageData.BarterStart(barterId, startMsg.getItemDescription());
	    	case BID_COMMITMENT:
	    		BidCommitmentMsg commitMsg = msg.getCommitment();
	    		BigInteger sharedVal = BarterProtoSerializer.takeBigInteger(commitMsg.getSharedVal());
	    		double r1 = commitMsg.getR1();
	    		byte[] hash =  commitMsg.getHash().toByteArray();
	    		return new OfferSubmission(barterId, hash, r1, sharedVal);
	    	case BID_COMPARISON:
	    		BidComparisonMsg compareMsg = msg.getComparison();
				boolean needReturnTesting = compareMsg.getNeedReturnComparison();
				BigInteger p = BarterProtoSerializer.takeBigInteger(compareMsg.getPrime());
	
				int count = compareMsg.getValuesCount();
				BigInteger[] z = new BigInteger[count];
				int i= 0;
                java.util.List<BigIntegerMsg> valuesList = compareMsg.getValuesList();
                for (int i1 = 0; i1 < valuesList.size(); i1++) {
                    BigIntegerMsg bigIntMsg = valuesList.get(i1);
                    z[i++] = BarterProtoSerializer.takeBigInteger(bigIntMsg);
                }
				return new ExchangeData(barterId, p, z, needReturnTesting);
	    	case BIDDING_OVER:
	    		return new BarterMessageData.BiddingOver(barterId);
			case BID_RECEIPT:
				return new BarterMessageData.BidReceipt(barterId);
	    	case CLAIM_WIN:
	    		RevealBidMsg conveyMsg = msg.getReveal();
	    		int bid = conveyMsg.getBid();
	    		BigInteger x = takeBigInteger(conveyMsg.getX());
	    		r1 = conveyMsg.getR1();
	    		double r2 = conveyMsg.getR2();
	    		return new BidConveyData(barterId, bid, x, r1, r2);
	    	case CONCEDE:
	    		return new BarterMessageData.Concession(barterId);
	    	case AUCTION_END:
	    		AuctionEndMsg endMsg = msg.getEnd();
	    		return new BarterEnd(barterId, endMsg.getWinner(), endMsg.getWinningBid());
	    		default:
	    			throw new IOException("Error deserializing message, unknown type " +msg.getType());
	    	}
		}
    	catch(Exception e){
    		throw new IOException("Error deserializing message " + e.getMessage());
    	}
    		
	}
	
    private static BigIntegerMsg makeBigIntegerMsg(BigInteger val){
    	return  BigIntegerMsg.newBuilder().setValue(ByteString.copyFrom(val.toByteArray())).build(); 
    }
    
    public static BigInteger takeBigInteger(BigIntegerMsg msg){
    	return new BigInteger(msg.getValue().toByteArray());
    }
}

