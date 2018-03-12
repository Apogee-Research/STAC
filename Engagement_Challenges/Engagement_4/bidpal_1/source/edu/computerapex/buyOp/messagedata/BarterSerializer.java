package edu.computerapex.buyOp.messagedata;

import edu.computerapex.buyOp.messagedata.BarterMessageData.BarterEnd;
import edu.computerapex.buyOp.messagedata.BarterMessageData.BarterStart;
import edu.computerapex.buyOp.messagedata.BarterMessageData.BidReceipt;
import edu.computerapex.buyOp.messagedata.BarterMessageData.BiddingOver;
import edu.computerapex.buyOp.messagedata.BarterMessageData.Concession;

import java.io.IOException;
import java.io.NotSerializableException;

/**
 * Encapsulates the transformation between bytes sent/received and AuctionMessageData.
 * (Envision having multiple formats.)
 */
public abstract class BarterSerializer {
	public abstract byte[] serialize(BarterStart data);
	public abstract byte[] serialize(BidCommitmentData data);
	public abstract byte[] serialize(ExchangeData data);
    public abstract byte[] serialize(BidReceipt data);
	public abstract byte[] serialize(BiddingOver data);
	public abstract byte[] serialize(Concession data);
	public abstract byte[] serialize(BidDivulgeData data);
	public abstract byte[] serialize(BarterEnd data);
	
	public byte[] serialize(BarterMessageData msg) throws NotSerializableException {
		if (msg instanceof BarterStart) {
			return serialize((BarterStart)msg);
		}else if (msg instanceof BidCommitmentData){
			return serialize((BidCommitmentData)msg);
		}else if (msg instanceof BidReceipt){
			return serialize((BidReceipt)msg);
		}else if (msg instanceof ExchangeData){
			return serialize((ExchangeData)msg);
		}else if (msg instanceof BiddingOver){
			return serialize((BiddingOver)msg);
		}else if (msg instanceof Concession){
			return serialize((Concession)msg);
		}else if (msg instanceof BidDivulgeData){
			return serialize((BidDivulgeData)msg);
		}else if (msg instanceof BarterEnd){
			return serialize((BarterEnd)msg);
		}else throw new NotSerializableException("BidPalSerializer received BidPalMessageData of unsupported type " + msg.type);
	}
	
	public abstract BarterMessageData deserialize(byte[] msg) throws IOException;
}

