package org.techpoint.sale.messagedata;

import org.techpoint.sale.AuctionProtos;
import org.techpoint.sale.AuctionProtos.AuctionEndMsg;
import org.techpoint.sale.AuctionProtos.AuctionMsg;
import org.techpoint.sale.AuctionProtos.AuctionMsg.Type;
import org.techpoint.sale.AuctionProtos.AuctionStartMsg;
import org.techpoint.sale.AuctionProtos.BidCommitmentMsg;
import org.techpoint.sale.AuctionProtos.BidComparisonMsg;
import org.techpoint.sale.AuctionProtos.BigIntegerMsg;
import org.techpoint.sale.AuctionProtos.RevealBidMsg;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Transforms AuctionMessageData to byte[] and back via the Google protocol buffers in AuctionProtos.
 */
public class AuctionProtoSerializer extends AuctionSerializer{
	public byte[] serialize(BidCommitmentData data){
		byte[] bytes = null;

		BigIntegerMsg bint = AuctionProtoSerializer.makeBigIntegerMsg(data.obtainSharedVal());
    	BidCommitmentMsg commit = 
    		AuctionProtos.BidCommitmentMsg.newBuilder()
    		.setHash(ByteString.copyFrom(data.obtainHash()))
    		.setR1(data.grabR1())
    		.setSharedVal(bint)
    		.build();
    	AuctionMsg msg = AuctionMsg.newBuilder()
    	    	.setAuctionId(data.getAuctionId())
    			.setCommitment(commit)
    			.setType(AuctionMsg.Type.BID_COMMITMENT)
    			.build();
    	bytes =  msg.toByteArray();

    	return bytes;
	}
	
	public byte[] serialize(BidComparisonData data){
		BigIntegerMsg pMsg = AuctionProtoSerializer.makeBigIntegerMsg(data.grabP());
		BidComparisonMsg.Builder compBuilder = BidComparisonMsg.newBuilder();
		compBuilder.setPrime(pMsg);
		compBuilder.setNeedReturnComparison(data.grabNeedReturn());

        for (int j =0; j < data.grabZLength(); ) {
            for (; (j < data.grabZLength()) && (Math.random() < 0.5); j++) {
                BigIntegerMsg vMsg = AuctionProtoSerializer.makeBigIntegerMsg(data.grabZ(j));
                compBuilder.addValues(vMsg);
            }
        }
		AuctionMsg msg = AuctionMsg.newBuilder()
			.setAuctionId(data.getAuctionId())
			.setType(Type.BID_COMPARISON)
			.setComparison(compBuilder.build())
			.build();
		return msg.toByteArray();
		
	}
	
	public byte[] serialize(ProposalReportData data){
		 RevealBidMsg claim = RevealBidMsg.newBuilder()
		 	.setX(AuctionProtoSerializer.makeBigIntegerMsg(data.pullX()))
		 	.setR1(data.getR1())
		 	.setR2(data.takeR2())
		 	.setBid(data.getProposal())
		 	.build();
		 AuctionMsg msg =AuctionMsg.newBuilder()
		 .setAuctionId(data.getAuctionId())
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
	   		.setAuctionId(auctionStart.getAuctionId())
	   		.setType(AuctionMsg.Type.AUCTION_START)
	   		.setStart(startMsg)
	   		.build();
			return msg.toByteArray();
		
	}

	public byte[] serialize(AuctionMessageData.ProposalReceipt proposalReceipt){
		AuctionMsg msg = AuctionMsg.newBuilder()
				.setAuctionId(proposalReceipt.getAuctionId())
				.setType(AuctionMsg.Type.BID_RECEIPT)
				.build();
		return msg.toByteArray();
	}
	
	public byte[] serialize(AuctionMessageData.BiddingOver overData){
		AuctionMsg msg = AuctionMsg.newBuilder()
		.setAuctionId(overData.getAuctionId())
		.setType(AuctionMsg.Type.BIDDING_OVER)
		.build();
		return msg.toByteArray();
	}
	
	public byte[] serialize(AuctionMessageData.Concession concession){
		AuctionMsg msg = AuctionMsg.newBuilder()
		.setAuctionId(concession.getAuctionId())
		.setType(AuctionMsg.Type.CONCEDE)
		.build();
		return msg.toByteArray();
	}
	
	public  byte[] serialize(AuctionMessageData.AuctionEnd endData){

		AuctionEndMsg endMsg = AuctionEndMsg.newBuilder()	
    		.setWinner(endData.winner)
    		.setWinningBid(endData.winningProposal)
    		.build();
		AuctionMsg msg = AuctionMsg.newBuilder()
    		.setAuctionId(endData.getAuctionId())
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
	    		BigInteger sharedVal = AuctionProtoSerializer.fetchBigInteger(commitMsg.getSharedVal());
	    		double r1 = commitMsg.getR1();
	    		byte[] hash =  commitMsg.getHash().toByteArray();
	    		return new BidCommitmentData(auctionId, hash, r1, sharedVal);
	    	case BID_COMPARISON:
	    		BidComparisonMsg compareMsg = msg.getComparison();
				boolean needReturnDistinguisher = compareMsg.getNeedReturnComparison();
				BigInteger p = AuctionProtoSerializer.fetchBigInteger(compareMsg.getPrime());
	
				int count = compareMsg.getValuesCount();
				BigInteger[] z = new BigInteger[count];
				int k = 0;
                java.util.List<BigIntegerMsg> valuesList = compareMsg.getValuesList();
                for (int i1 = 0; i1 < valuesList.size(); i1++) {
                    BigIntegerMsg bigIntMsg = valuesList.get(i1);
                    z[k++] = AuctionProtoSerializer.fetchBigInteger(bigIntMsg);
                }
				return new BidComparisonData(auctionId, p, z, needReturnDistinguisher);
	    	case BIDDING_OVER:
	    		return new AuctionMessageData.BiddingOver(auctionId);
			case BID_RECEIPT:
				return new AuctionMessageData.ProposalReceipt(auctionId);
	    	case CLAIM_WIN:
	    		RevealBidMsg reportMsg = msg.getReveal();
	    		int proposal = reportMsg.getBid();
	    		BigInteger x = fetchBigInteger(reportMsg.getX());
	    		r1 = reportMsg.getR1();
	    		double r2 = reportMsg.getR2();
	    		return new ProposalReportData(auctionId, proposal, x, r1, r2);
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
    
    public static BigInteger fetchBigInteger(BigIntegerMsg msg){
    	return new BigInteger(msg.getValue().toByteArray());
    }
}

