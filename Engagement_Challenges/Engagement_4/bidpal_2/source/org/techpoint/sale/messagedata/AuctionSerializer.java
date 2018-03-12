package org.techpoint.sale.messagedata;

import java.io.IOException;
import java.io.NotSerializableException;

/**
 * Encapsulates the transformation between bytes sent/received and AuctionMessageData.
 * (Envision having multiple formats.)
 */
public abstract class AuctionSerializer {
	public abstract byte[] serialize(AuctionMessageData.AuctionStart data);
	public abstract byte[] serialize(BidCommitmentData data);
	public abstract byte[] serialize(BidComparisonData data);
    public abstract byte[] serialize(AuctionMessageData.ProposalReceipt data);
	public abstract byte[] serialize(AuctionMessageData.BiddingOver data);
	public abstract byte[] serialize(AuctionMessageData.Concession data);
	public abstract byte[] serialize(ProposalReportData data);
	public abstract byte[] serialize(AuctionMessageData.AuctionEnd data);
	
	public byte[] serialize(AuctionMessageData msg) throws NotSerializableException {
		if (msg instanceof AuctionMessageData.AuctionStart) {
			return serialize((AuctionMessageData.AuctionStart)msg);
		}else if (msg instanceof BidCommitmentData){
			return serialize((BidCommitmentData)msg);
		}else if (msg instanceof AuctionMessageData.ProposalReceipt){
			return serialize((AuctionMessageData.ProposalReceipt)msg);
		}else if (msg instanceof BidComparisonData){
			return serialize((BidComparisonData)msg);
		}else if (msg instanceof AuctionMessageData.BiddingOver){
			return serialize((AuctionMessageData.BiddingOver)msg);
		}else if (msg instanceof AuctionMessageData.Concession){
			return serialize((AuctionMessageData.Concession)msg);
		}else if (msg instanceof ProposalReportData){
			return serialize((ProposalReportData)msg);
		}else if (msg instanceof AuctionMessageData.AuctionEnd){
			return serialize((AuctionMessageData.AuctionEnd)msg);
		}else throw new NotSerializableException("BidPalSerializer received BidPalMessageData of unsupported type " + msg.type);
	}
	
	public abstract AuctionMessageData deserialize(byte[] msg) throws IOException;
}

