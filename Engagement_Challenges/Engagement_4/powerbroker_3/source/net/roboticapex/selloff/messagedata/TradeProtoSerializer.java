package net.roboticapex.selloff.messagedata;

import net.roboticapex.selloff.AuctionProtos;
import net.roboticapex.selloff.AuctionProtos.AuctionEndMsg;
import net.roboticapex.selloff.AuctionProtos.AuctionMsg;
import net.roboticapex.selloff.AuctionProtos.AuctionMsg.Type;
import net.roboticapex.selloff.AuctionProtos.AuctionStartMsg;
import net.roboticapex.selloff.AuctionProtos.BidCommitmentMsg;
import net.roboticapex.selloff.AuctionProtos.BidComparisonMsg;
import net.roboticapex.selloff.AuctionProtos.BigIntegerMsg;
import net.roboticapex.selloff.AuctionProtos.RevealBidMsg;
import net.roboticapex.selloff.messagedata.TradeMessageData.TradeEnd;
import net.roboticapex.selloff.messagedata.TradeMessageData.BiddingOver;
import net.roboticapex.selloff.messagedata.TradeMessageData.Concession;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.math.BigInteger;


/**
 * Transforms AuctionMessageData to byte[] and back via the Google protocol buffers in AuctionProtos.
 */
public class TradeProtoSerializer extends TradeSerializer {
	public byte[] serialize(BidCommitmentData data){
		byte[] bytes = null;

		BigIntegerMsg bint = TradeProtoSerializer.makeBigIntegerMsg(data.obtainSharedVal());
    	BidCommitmentMsg commit = 
    		AuctionProtos.BidCommitmentMsg.newBuilder()
    		.setHash(ByteString.copyFrom(data.obtainHash()))
    		.setR1(data.obtainR1())
    		.setSharedVal(bint)
    		.build();
    	AuctionMsg msg = AuctionMsg.newBuilder()
    	    	.setAuctionId(data.obtainTradeId())
    			.setCommitment(commit)
    			.setType(AuctionMsg.Type.BID_COMMITMENT)
    			.build();
    	bytes =  msg.toByteArray();

    	return bytes;
	}
	
	public byte[] serialize(TestData data){
		BigIntegerMsg pMsg = TradeProtoSerializer.makeBigIntegerMsg(data.getP());
		BidComparisonMsg.Builder compBuilder = BidComparisonMsg.newBuilder();
		compBuilder.setPrime(pMsg);
		compBuilder.setNeedReturnComparison(data.pullNeedReturn());
		
		for (int j =0; j < data.fetchZLength(); j++){
			BigIntegerMsg vMsg = TradeProtoSerializer.makeBigIntegerMsg(data.grabZ(j));
			compBuilder.addValues(vMsg); 
		}
		AuctionMsg msg = AuctionMsg.newBuilder()
			.setAuctionId(data.obtainTradeId())
			.setType(Type.BID_COMPARISON)
			.setComparison(compBuilder.build())
			.build();
		return msg.toByteArray();
		
	}
	
	public byte[] serialize(PromiseDivulgeData data){
		 RevealBidMsg claim = RevealBidMsg.newBuilder()
		 	.setX(TradeProtoSerializer.makeBigIntegerMsg(data.getX()))
		 	.setR1(data.pullR1())
		 	.setR2(data.fetchR2())
		 	.setBid(data.obtainPromise())
		 	.build();
		 AuctionMsg msg =AuctionMsg.newBuilder()
		 .setAuctionId(data.obtainTradeId())
		 .setType(AuctionMsg.Type.CLAIM_WIN)
		 .setReveal(claim)
		 .build();
		 return msg.toByteArray();
	}
	
	public byte[] serialize(TradeMessageData.TradeStart tradeStart){

    	 AuctionStartMsg startMsg = AuctionStartMsg.newBuilder()
		.setItemDescription(tradeStart.description)
		.build();
	
    	 AuctionMsg msg = AuctionMsg.newBuilder()
	   		.setAuctionId(tradeStart.obtainTradeId())
	   		.setType(AuctionMsg.Type.AUCTION_START)
	   		.setStart(startMsg)
	   		.build();
			return msg.toByteArray();
		
	}

	public byte[] serialize(TradeMessageData.PromiseReceipt promiseReceipt){
		AuctionMsg msg = AuctionMsg.newBuilder()
				.setAuctionId(promiseReceipt.obtainTradeId())
				.setType(AuctionMsg.Type.BID_RECEIPT)
				.build();
		return msg.toByteArray();
	}
	
	public byte[] serialize(BiddingOver overData){
		AuctionMsg msg = AuctionMsg.newBuilder()
		.setAuctionId(overData.obtainTradeId())
		.setType(AuctionMsg.Type.BIDDING_OVER)
		.build();
		return msg.toByteArray();
	}
	
	public byte[] serialize(Concession concession){
		AuctionMsg msg = AuctionMsg.newBuilder()
		.setAuctionId(concession.obtainTradeId())
		.setType(AuctionMsg.Type.CONCEDE)
		.build();
		return msg.toByteArray();
	}
	
	public  byte[] serialize(TradeEnd endData){

		AuctionEndMsg endMsg = AuctionEndMsg.newBuilder()	
    		.setWinner(endData.winner)
    		.setWinningBid(endData.winningPromise)
    		.build();
		AuctionMsg msg = AuctionMsg.newBuilder()
    		.setAuctionId(endData.obtainTradeId())
    		.setType(AuctionMsg.Type.AUCTION_END)
    		.setEnd(endMsg)
    		.build();
    	return msg.toByteArray();

	}
	
	public TradeMessageData deserialize(byte[] bytes) throws IOException{
		try{
			AuctionMsg msg = AuctionMsg.parseFrom(bytes);
	    	String tradeId = msg.getAuctionId();
	    	switch(msg.getType()){
	    	case AUCTION_START:
	    		AuctionStartMsg startMsg = msg.getStart();
	    		return new TradeMessageData.TradeStart(tradeId, startMsg.getItemDescription());
	    	case BID_COMMITMENT:
	    		BidCommitmentMsg commitMsg = msg.getCommitment();
	    		BigInteger sharedVal = TradeProtoSerializer.pullBigInteger(commitMsg.getSharedVal());
	    		double r1 = commitMsg.getR1();
	    		byte[] hash =  commitMsg.getHash().toByteArray();
	    		return new BidCommitmentData(tradeId, hash, r1, sharedVal);
	    	case BID_COMPARISON:
	    		BidComparisonMsg compareMsg = msg.getComparison();
				boolean needReturnTesting = compareMsg.getNeedReturnComparison();
				BigInteger p = TradeProtoSerializer.pullBigInteger(compareMsg.getPrime());
	
				int count = compareMsg.getValuesCount();
				BigInteger[] z = new BigInteger[count];
				int b = 0;
                java.util.List<BigIntegerMsg> valuesList = compareMsg.getValuesList();
                for (int i1 = 0; i1 < valuesList.size(); i1++) {
                    BigIntegerMsg bigIntMsg = valuesList.get(i1);
                    z[b++] = TradeProtoSerializer.pullBigInteger(bigIntMsg);
                }
				return new TestData(tradeId, p, z, needReturnTesting);
	    	case BIDDING_OVER:
	    		return new TradeMessageData.BiddingOver(tradeId);
			case BID_RECEIPT:
				return new TradeMessageData.PromiseReceipt(tradeId);
	    	case CLAIM_WIN:
	    		RevealBidMsg divulgeMsg = msg.getReveal();
	    		int promise = divulgeMsg.getBid();
	    		BigInteger x = pullBigInteger(divulgeMsg.getX());
	    		r1 = divulgeMsg.getR1();
	    		double r2 = divulgeMsg.getR2();
	    		return new PromiseDivulgeData(tradeId, promise, x, r1, r2);
	    	case CONCEDE:
	    		return new TradeMessageData.Concession(tradeId);
	    	case AUCTION_END:
	    		AuctionEndMsg endMsg = msg.getEnd();
	    		return new TradeEnd(tradeId, endMsg.getWinner(), endMsg.getWinningBid());
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
    
    public static BigInteger pullBigInteger(BigIntegerMsg msg){
    	return new BigInteger(msg.getValue().toByteArray());
    }
}

