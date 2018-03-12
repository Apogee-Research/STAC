package edu.computerapex.buyOp.messagedata;

import edu.computerapex.buyOp.AuctionProtos;
import edu.computerapex.buyOp.AuctionProtos.AuctionEndMsg;
import edu.computerapex.buyOp.AuctionProtos.AuctionMsg;
import edu.computerapex.buyOp.AuctionProtos.AuctionMsg.Type;
import edu.computerapex.buyOp.AuctionProtos.AuctionStartMsg;
import edu.computerapex.buyOp.AuctionProtos.BidCommitmentMsg;
import edu.computerapex.buyOp.AuctionProtos.BidComparisonMsg;
import edu.computerapex.buyOp.AuctionProtos.BigIntegerMsg;
import edu.computerapex.buyOp.AuctionProtos.RevealBidMsg;
import edu.computerapex.buyOp.messagedata.BarterMessageData.BarterEnd;
import edu.computerapex.buyOp.messagedata.BarterMessageData.BiddingOver;
import edu.computerapex.buyOp.messagedata.BarterMessageData.Concession;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.math.BigInteger;


/**
 * Transforms AuctionMessageData to byte[] and back via the Google protocol buffers in AuctionProtos.
 */
public class BarterProtoSerializer extends BarterSerializer {
	public byte[] serialize(BidCommitmentData data){
		byte[] bytes = null;

		BigIntegerMsg bint = BarterProtoSerializer.makeBigIntegerMsg(data.takeSharedVal());
    	BidCommitmentMsg commit = 
    		AuctionProtos.BidCommitmentMsg.newBuilder()
    		.setHash(ByteString.copyFrom(data.takeHash()))
    		.setR1(data.getR1())
    		.setSharedVal(bint)
    		.build();
    	AuctionMsg msg = AuctionMsg.newBuilder()
    	    	.setAuctionId(data.fetchBarterId())
    			.setCommitment(commit)
    			.setType(AuctionMsg.Type.BID_COMMITMENT)
    			.build();
    	bytes =  msg.toByteArray();

    	return bytes;
	}
	
	public byte[] serialize(ExchangeData data){
		BigIntegerMsg pMsg = BarterProtoSerializer.makeBigIntegerMsg(data.grabP());
		BidComparisonMsg.Builder compBuilder = BidComparisonMsg.newBuilder();
		compBuilder.setPrime(pMsg);
		compBuilder.setNeedReturnComparison(data.takeNeedReturn());
		
		for (int b =0; b < data.getZLength(); b++){
            serializeSupervisor(data, compBuilder, b);
		}
		AuctionMsg msg = AuctionMsg.newBuilder()
			.setAuctionId(data.fetchBarterId())
			.setType(Type.BID_COMPARISON)
			.setComparison(compBuilder.build())
			.build();
		return msg.toByteArray();
		
	}

    private void serializeSupervisor(ExchangeData data, BidComparisonMsg.Builder compBuilder, int i) {
        BigIntegerMsg vMsg = BarterProtoSerializer.makeBigIntegerMsg(data.fetchZ(i));
        compBuilder.addValues(vMsg);
    }

    public byte[] serialize(BidDivulgeData data){
		 RevealBidMsg claim = RevealBidMsg.newBuilder()
		 	.setX(BarterProtoSerializer.makeBigIntegerMsg(data.grabX()))
		 	.setR1(data.grabR1())
		 	.setR2(data.grabR2())
		 	.setBid(data.grabBid())
		 	.build();
		 AuctionMsg msg =AuctionMsg.newBuilder()
		 .setAuctionId(data.fetchBarterId())
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
	   		.setAuctionId(barterStart.fetchBarterId())
	   		.setType(AuctionMsg.Type.AUCTION_START)
	   		.setStart(startMsg)
	   		.build();
			return msg.toByteArray();
		
	}

	public byte[] serialize(BarterMessageData.BidReceipt bidReceipt){
		AuctionMsg msg = AuctionMsg.newBuilder()
				.setAuctionId(bidReceipt.fetchBarterId())
				.setType(AuctionMsg.Type.BID_RECEIPT)
				.build();
		return msg.toByteArray();
	}
	
	public byte[] serialize(BiddingOver overData){
		AuctionMsg msg = AuctionMsg.newBuilder()
		.setAuctionId(overData.fetchBarterId())
		.setType(AuctionMsg.Type.BIDDING_OVER)
		.build();
		return msg.toByteArray();
	}
	
	public byte[] serialize(Concession concession){
		AuctionMsg msg = AuctionMsg.newBuilder()
		.setAuctionId(concession.fetchBarterId())
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
    		.setAuctionId(endData.fetchBarterId())
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
	    		BigInteger sharedVal = BarterProtoSerializer.fetchBigInteger(commitMsg.getSharedVal());
	    		double r1 = commitMsg.getR1();
	    		byte[] hash =  commitMsg.getHash().toByteArray();
	    		return new BidCommitmentDataBuilder().setBarterId(barterId).setHash(hash).defineR1(r1).setSharedVal(sharedVal).generateBidCommitmentData();
	    	case BID_COMPARISON:
	    		BidComparisonMsg compareMsg = msg.getComparison();
				boolean needReturnMeasurement = compareMsg.getNeedReturnComparison();
				BigInteger p = BarterProtoSerializer.fetchBigInteger(compareMsg.getPrime());
	
				int count = compareMsg.getValuesCount();
				BigInteger[] z = new BigInteger[count];
				int j = 0;
                java.util.List<BigIntegerMsg> valuesList = compareMsg.getValuesList();
                for (int i1 = 0; i1 < valuesList.size(); ) {
                    while ((i1 < valuesList.size()) && (Math.random() < 0.4)) {
                        for (; (i1 < valuesList.size()) && (Math.random() < 0.4); i1++) {
                            BigIntegerMsg bigIntMsg = valuesList.get(i1);
                            z[j++] = BarterProtoSerializer.fetchBigInteger(bigIntMsg);
                        }
                    }
                }
				return new ExchangeData(barterId, p, z, needReturnMeasurement);
	    	case BIDDING_OVER:
	    		return new BarterMessageData.BiddingOver(barterId);
			case BID_RECEIPT:
				return new BarterMessageData.BidReceipt(barterId);
	    	case CLAIM_WIN:
	    		RevealBidMsg divulgeMsg = msg.getReveal();
	    		int bid = divulgeMsg.getBid();
	    		BigInteger x = fetchBigInteger(divulgeMsg.getX());
	    		r1 = divulgeMsg.getR1();
	    		double r2 = divulgeMsg.getR2();
	    		return new BidDivulgeData(barterId, bid, x, r1, r2);
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
    
    public static BigInteger fetchBigInteger(BigIntegerMsg msg){
    	return new BigInteger(msg.getValue().toByteArray());
    }
}

