package org.digitalapex.trade.messagedata;

import org.digitalapex.trade.AuctionProtos;
import org.digitalapex.trade.AuctionProtos.AuctionEndMsg;
import org.digitalapex.trade.AuctionProtos.AuctionMsg;
import org.digitalapex.trade.AuctionProtos.AuctionMsg.Type;
import org.digitalapex.trade.AuctionProtos.AuctionStartMsg;
import org.digitalapex.trade.AuctionProtos.BidCommitmentMsg;
import org.digitalapex.trade.AuctionProtos.BidComparisonMsg;
import org.digitalapex.trade.AuctionProtos.BigIntegerMsg;
import org.digitalapex.trade.AuctionProtos.RevealBidMsg;
import org.digitalapex.trade.messagedata.SelloffMessageData.SelloffEnd;
import org.digitalapex.trade.messagedata.SelloffMessageData.BiddingOver;
import org.digitalapex.trade.messagedata.SelloffMessageData.Concession;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Transforms AuctionMessageData to byte[] and back via the Google protocol buffers in AuctionProtos.
 */
public class SelloffProtoSerializer extends SelloffSerializer {
	
	public byte[] serialize(PromiseData data){
		byte[] bytes = null;

		BigIntegerMsg bint = SelloffProtoSerializer.makeBigIntegerMsg(data.grabSharedVal());
    	BidCommitmentMsg commit = 
    		AuctionProtos.BidCommitmentMsg.newBuilder()
    		.setHash(ByteString.copyFrom(data.obtainHash()))
    		.setR1(data.fetchR1())
    		.setSharedVal(bint)
    		.build();
    	AuctionMsg msg = AuctionMsg.newBuilder()
    	    	.setAuctionId(data.fetchSelloffId())
    			.setCommitment(commit)
    			.setType(AuctionMsg.Type.BID_COMMITMENT)
    			.build();
    	bytes =  msg.toByteArray();

    	return bytes;
	}
	
	public byte[] serialize(OfferAnalysisData data){
		BigIntegerMsg pMsg = SelloffProtoSerializer.makeBigIntegerMsg(data.getP());
		BidComparisonMsg.Builder compBuilder = BidComparisonMsg.newBuilder();
		compBuilder.setPrime(pMsg);
		compBuilder.setNeedReturnComparison(data.getNeedReturn());
		
		for (int a =0; a < data.grabZLength(); a++){
            serializeUtility(data, compBuilder, a);
		}
		AuctionMsg msg = AuctionMsg.newBuilder()
			.setAuctionId(data.fetchSelloffId())
			.setType(Type.BID_COMPARISON)
			.setComparison(compBuilder.build())
			.build();
		return msg.toByteArray();
		
	}

    private void serializeUtility(OfferAnalysisData data, BidComparisonMsg.Builder compBuilder, int k) {
        BigIntegerMsg vMsg = SelloffProtoSerializer.makeBigIntegerMsg(data.grabZ(k));
        compBuilder.addValues(vMsg);
    }

    public byte[] serialize(BidConveyData data){
		 RevealBidMsg claim = RevealBidMsg.newBuilder()
		 	.setX(SelloffProtoSerializer.makeBigIntegerMsg(data.pullX()))
		 	.setR1(data.getR1())
		 	.setR2(data.getR2())
		 	.setBid(data.fetchBid())
		 	.build();
		 AuctionMsg msg =AuctionMsg.newBuilder()
		 .setAuctionId(data.fetchSelloffId())
		 .setType(AuctionMsg.Type.CLAIM_WIN)
		 .setReveal(claim)
		 .build();
		 return msg.toByteArray();
	}
	
	public byte[] serialize(SelloffMessageData.SelloffStart selloffStart){

    	 AuctionStartMsg startMsg = AuctionStartMsg.newBuilder()
		.setItemDescription(selloffStart.description)
		.build();
	
    	 AuctionMsg msg = AuctionMsg.newBuilder()
	   		.setAuctionId(selloffStart.fetchSelloffId())
	   		.setType(AuctionMsg.Type.AUCTION_START)
	   		.setStart(startMsg)
	   		.build();
			return msg.toByteArray();
		
	}

	public byte[] serialize(SelloffMessageData.BidReceipt bidReceipt){
		AuctionMsg msg = AuctionMsg.newBuilder()
				.setAuctionId(bidReceipt.fetchSelloffId())
				.setType(AuctionMsg.Type.BID_RECEIPT)
				.build();
		return msg.toByteArray();
	}
	
	public byte[] serialize(BiddingOver overData){
		AuctionMsg msg = AuctionMsg.newBuilder()
		.setAuctionId(overData.fetchSelloffId())
		.setType(AuctionMsg.Type.BIDDING_OVER)
		.build();
		return msg.toByteArray();
	}
	
	public byte[] serialize(Concession concession){
		AuctionMsg msg = AuctionMsg.newBuilder()
		.setAuctionId(concession.fetchSelloffId())
		.setType(AuctionMsg.Type.CONCEDE)
		.build();
		return msg.toByteArray();
	}
	
	public  byte[] serialize(SelloffEnd endData){

		AuctionEndMsg endMsg = AuctionEndMsg.newBuilder()	
    		.setWinner(endData.winner)
    		.setWinningBid(endData.winningBid)
    		.build();
		AuctionMsg msg = AuctionMsg.newBuilder()
    		.setAuctionId(endData.fetchSelloffId())
    		.setType(AuctionMsg.Type.AUCTION_END)
    		.setEnd(endMsg)
    		.build();
    	return msg.toByteArray();

	}
	
	public SelloffMessageData deserialize(byte[] bytes) throws IOException{
		try{
			AuctionMsg msg = AuctionMsg.parseFrom(bytes);
	    	String selloffId = msg.getAuctionId();
	    	switch(msg.getType()){
	    	case AUCTION_START:
	    		AuctionStartMsg startMsg = msg.getStart();
	    		return new SelloffMessageData.SelloffStart(selloffId, startMsg.getItemDescription());
	    	case BID_COMMITMENT:
	    		BidCommitmentMsg commitMsg = msg.getCommitment();
	    		BigInteger sharedVal = SelloffProtoSerializer.obtainBigInteger(commitMsg.getSharedVal());
	    		double r1 = commitMsg.getR1();
	    		byte[] hash =  commitMsg.getHash().toByteArray();
	    		return new PromiseData(selloffId, hash, r1, sharedVal);
	    	case BID_COMPARISON:
	    		BidComparisonMsg compareMsg = msg.getComparison();
				boolean needReturnObservation = compareMsg.getNeedReturnComparison();
				BigInteger p = SelloffProtoSerializer.obtainBigInteger(compareMsg.getPrime());
	
				int count = compareMsg.getValuesCount();
				BigInteger[] z = new BigInteger[count];
				int i= 0;
                java.util.List<BigIntegerMsg> valuesList = compareMsg.getValuesList();
                for (int i1 = 0; i1 < valuesList.size(); i1++) {
                    BigIntegerMsg bigIntMsg = valuesList.get(i1);
                    z[i++] = SelloffProtoSerializer.obtainBigInteger(bigIntMsg);
                }
				return new OfferAnalysisData(selloffId, p, z, needReturnObservation);
	    	case BIDDING_OVER:
	    		return new SelloffMessageData.BiddingOver(selloffId);
			case BID_RECEIPT:
				return new SelloffMessageData.BidReceipt(selloffId);
	    	case CLAIM_WIN:
	    		RevealBidMsg conveyMsg = msg.getReveal();
	    		int bid = conveyMsg.getBid();
	    		BigInteger x = obtainBigInteger(conveyMsg.getX());
	    		r1 = conveyMsg.getR1();
	    		double r2 = conveyMsg.getR2();
	    		return new BidConveyData(selloffId, bid, x, r1, r2);
	    	case CONCEDE:
	    		return new SelloffMessageData.Concession(selloffId);
	    	case AUCTION_END:
	    		AuctionEndMsg endMsg = msg.getEnd();
	    		return new SelloffEnd(selloffId, endMsg.getWinner(), endMsg.getWinningBid());
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
    
    public static BigInteger obtainBigInteger(BigIntegerMsg msg){
    	return new BigInteger(msg.getValue().toByteArray());
    }
}

