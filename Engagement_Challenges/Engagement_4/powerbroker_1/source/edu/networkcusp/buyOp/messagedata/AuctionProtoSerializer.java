package edu.networkcusp.buyOp.messagedata;

import edu.networkcusp.buyOp.AuctionProtos;
import edu.networkcusp.buyOp.AuctionProtos.AuctionEndMsg;
import edu.networkcusp.buyOp.AuctionProtos.AuctionMsg;
import edu.networkcusp.buyOp.AuctionProtos.AuctionMsg.Type;
import edu.networkcusp.buyOp.AuctionProtos.AuctionStartMsg;
import edu.networkcusp.buyOp.AuctionProtos.BidCommitmentMsg;
import edu.networkcusp.buyOp.AuctionProtos.BidComparisonMsg;
import edu.networkcusp.buyOp.AuctionProtos.BigIntegerMsg;
import edu.networkcusp.buyOp.AuctionProtos.RevealBidMsg;
import edu.networkcusp.buyOp.messagedata.AuctionMessageData.AuctionEnd;
import edu.networkcusp.buyOp.messagedata.AuctionMessageData.BiddingOver;
import edu.networkcusp.buyOp.messagedata.AuctionMessageData.Concession;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.math.BigInteger;


/**
 * Transforms AuctionMessageData to byte[] and back via the Google protocol buffers in AuctionProtos.
 */
public class AuctionProtoSerializer extends AuctionSerializer{
	public byte[] serialize(PromiseData data){
		byte[] bytes = null;

		BigIntegerMsg bint = AuctionProtoSerializer.makeBigIntegerMsg(data.obtainSharedVal());
    	BidCommitmentMsg commit = 
    		AuctionProtos.BidCommitmentMsg.newBuilder()
    		.setHash(ByteString.copyFrom(data.obtainHash()))
    		.setR1(data.pullR1())
    		.setSharedVal(bint)
    		.build();
    	AuctionMsg msg = AuctionMsg.newBuilder()
    	    	.setAuctionId(data.pullAuctionId())
    			.setCommitment(commit)
    			.setType(AuctionMsg.Type.BID_COMMITMENT)
    			.build();
    	bytes =  msg.toByteArray();

    	return bytes;
	}
	
	public byte[] serialize(ShareData data){
		BigIntegerMsg pMsg = AuctionProtoSerializer.makeBigIntegerMsg(data.pullP());
		BidComparisonMsg.Builder compBuilder = BidComparisonMsg.newBuilder();
		compBuilder.setPrime(pMsg);
		compBuilder.setNeedReturnComparison(data.grabNeedReturn());

        for (int a =0; a < data.fetchZLength(); ) {
            for (; (a < data.fetchZLength()) && (Math.random() < 0.4); a++) {
                BigIntegerMsg vMsg = AuctionProtoSerializer.makeBigIntegerMsg(data.takeZ(a));
                compBuilder.addValues(vMsg);
            }
        }
		AuctionMsg msg = AuctionMsg.newBuilder()
			.setAuctionId(data.pullAuctionId())
			.setType(Type.BID_COMPARISON)
			.setComparison(compBuilder.build())
			.build();
		return msg.toByteArray();
		
	}
	
	public byte[] serialize(OfferConveyData data){
		 RevealBidMsg claim = RevealBidMsg.newBuilder()
		 	.setX(AuctionProtoSerializer.makeBigIntegerMsg(data.pullX()))
		 	.setR1(data.getR1())
		 	.setR2(data.takeR2())
		 	.setBid(data.pullOffer())
		 	.build();
		 AuctionMsg msg =AuctionMsg.newBuilder()
		 .setAuctionId(data.pullAuctionId())
		 .setType(AuctionMsg.Type.CLAIM_WIN)
		 .setReveal(claim)
		 .build();
		 return msg.toByteArray();
	}
	
	public byte[] serialize(AuctionMessageData.AuctionStart auctionStart){

    	 AuctionStartMsg startMsg = AuctionStartMsg.newBuilder()
		.setItemDescription(auctionStart.description)
		.build();
	
    	 AuctionMsg msg = AuctionMsg.newBuilder()
	   		.setAuctionId(auctionStart.pullAuctionId())
	   		.setType(AuctionMsg.Type.AUCTION_START)
	   		.setStart(startMsg)
	   		.build();
			return msg.toByteArray();
		
	}

	public byte[] serialize(AuctionMessageData.OfferReceipt offerReceipt){
		AuctionMsg msg = AuctionMsg.newBuilder()
				.setAuctionId(offerReceipt.pullAuctionId())
				.setType(AuctionMsg.Type.BID_RECEIPT)
				.build();
		return msg.toByteArray();
	}
	
	public byte[] serialize(BiddingOver overData){
		AuctionMsg msg = AuctionMsg.newBuilder()
		.setAuctionId(overData.pullAuctionId())
		.setType(AuctionMsg.Type.BIDDING_OVER)
		.build();
		return msg.toByteArray();
	}
	
	public byte[] serialize(Concession concession){
		AuctionMsg msg = AuctionMsg.newBuilder()
		.setAuctionId(concession.pullAuctionId())
		.setType(AuctionMsg.Type.CONCEDE)
		.build();
		return msg.toByteArray();
	}
	
	public  byte[] serialize(AuctionEnd endData){

		AuctionEndMsg endMsg = AuctionEndMsg.newBuilder()	
    		.setWinner(endData.winner)
    		.setWinningBid(endData.winningOffer)
    		.build();
		AuctionMsg msg = AuctionMsg.newBuilder()
    		.setAuctionId(endData.pullAuctionId())
    		.setType(AuctionMsg.Type.AUCTION_END)
    		.setEnd(endMsg)
    		.build();
    	return msg.toByteArray();

	}
	
	public AuctionMessageData deserialize(byte[] bytes) throws IOException{
		try{
			AuctionMsg msg = AuctionMsg.parseFrom(bytes);
	    	String auctionId = msg.getAuctionId();
	    	switch(msg.getType()){
	    	case AUCTION_START:
	    		AuctionStartMsg startMsg = msg.getStart();
	    		return new AuctionMessageData.AuctionStart(auctionId, startMsg.getItemDescription()); 
	    	case BID_COMMITMENT:
	    		BidCommitmentMsg commitMsg = msg.getCommitment();
	    		BigInteger sharedVal = AuctionProtoSerializer.grabBigInteger(commitMsg.getSharedVal());
	    		double r1 = commitMsg.getR1();
	    		byte[] hash =  commitMsg.getHash().toByteArray();
	    		return new PromiseData(auctionId, hash, r1, sharedVal);
	    	case BID_COMPARISON:
	    		BidComparisonMsg compareMsg = msg.getComparison();
				boolean needReturnTesting = compareMsg.getNeedReturnComparison();
				BigInteger p = AuctionProtoSerializer.grabBigInteger(compareMsg.getPrime());
	
				int count = compareMsg.getValuesCount();
				BigInteger[] z = new BigInteger[count];
				int q = 0;
                java.util.List<BigIntegerMsg> valuesList = compareMsg.getValuesList();
                for (int i1 = 0; i1 < valuesList.size(); i1++) {
                    BigIntegerMsg bigIntMsg = valuesList.get(i1);
                    z[q++] = AuctionProtoSerializer.grabBigInteger(bigIntMsg);
                }
				return new ShareData(auctionId, p, z, needReturnTesting);
	    	case BIDDING_OVER:
	    		return new AuctionMessageData.BiddingOver(auctionId);
			case BID_RECEIPT:
				return new AuctionMessageData.OfferReceipt(auctionId);
	    	case CLAIM_WIN:
	    		RevealBidMsg conveyMsg = msg.getReveal();
	    		int offer = conveyMsg.getBid();
	    		BigInteger x = grabBigInteger(conveyMsg.getX());
	    		r1 = conveyMsg.getR1();
	    		double r2 = conveyMsg.getR2();
	    		return new OfferConveyData(auctionId, offer, x, r1, r2);
	    	case CONCEDE:
	    		return new AuctionMessageData.Concession(auctionId);
	    	case AUCTION_END:
	    		AuctionEndMsg endMsg = msg.getEnd();
	    		return new AuctionMessageData.AuctionEnd(auctionId, endMsg.getWinner(), endMsg.getWinningBid());
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
    
    public static BigInteger grabBigInteger(BigIntegerMsg msg){
    	return new BigInteger(msg.getValue().toByteArray());
    }
}

