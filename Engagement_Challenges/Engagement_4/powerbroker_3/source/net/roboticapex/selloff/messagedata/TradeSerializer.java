package net.roboticapex.selloff.messagedata;

import java.io.IOException;
import java.io.NotSerializableException;

/**
 * Encapsulates the transformation between bytes sent/received and AuctionMessageData.
 * (Envision having multiple formats.)
 */
public abstract class TradeSerializer {
	public abstract byte[] serialize(TradeMessageData.TradeStart data);
	public abstract byte[] serialize(BidCommitmentData data);
	public abstract byte[] serialize(TestData data);
    public abstract byte[] serialize(TradeMessageData.PromiseReceipt data);
	public abstract byte[] serialize(TradeMessageData.BiddingOver data);
	public abstract byte[] serialize(TradeMessageData.Concession data);
	public abstract byte[] serialize(PromiseDivulgeData data);
	public abstract byte[] serialize(TradeMessageData.TradeEnd data);
	
	public byte[] serialize(TradeMessageData msg) throws NotSerializableException {
		if (msg instanceof TradeMessageData.TradeStart) {
			return serialize((TradeMessageData.TradeStart)msg);
		}else if (msg instanceof BidCommitmentData){
			return serialize((BidCommitmentData)msg);
		}else if (msg instanceof TradeMessageData.PromiseReceipt){
			return serialize((TradeMessageData.PromiseReceipt)msg);
		}else if (msg instanceof TestData){
			return serialize((TestData)msg);
		}else if (msg instanceof TradeMessageData.BiddingOver){
			return serialize((TradeMessageData.BiddingOver)msg);
		}else if (msg instanceof TradeMessageData.Concession){
			return serialize((TradeMessageData.Concession)msg);
		}else if (msg instanceof PromiseDivulgeData){
			return serialize((PromiseDivulgeData)msg);
		}else if (msg instanceof TradeMessageData.TradeEnd){
			return serialize((TradeMessageData.TradeEnd)msg);
		}else throw new NotSerializableException("BidPalSerializer received BidPalMessageData of unsupported type " + msg.type);
	}
	
	public abstract TradeMessageData deserialize(byte[] msg) throws IOException;
}

