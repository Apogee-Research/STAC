package org.digitalapex.trade.messagedata;

import org.digitalapex.trade.messagedata.SelloffMessageData.SelloffEnd;
import org.digitalapex.trade.messagedata.SelloffMessageData.SelloffStart;
import org.digitalapex.trade.messagedata.SelloffMessageData.BidReceipt;
import org.digitalapex.trade.messagedata.SelloffMessageData.BiddingOver;
import org.digitalapex.trade.messagedata.SelloffMessageData.Concession;

import java.io.IOException;
import java.io.NotSerializableException;

/**
 * Encapsulates the transformation between bytes sent/received and AuctionMessageData.
 * (Envision having multiple formats.)
 */
public abstract class SelloffSerializer {
	public abstract byte[] serialize(SelloffStart data);
	public abstract byte[] serialize(PromiseData data);
	public abstract byte[] serialize(OfferAnalysisData data);
    public abstract byte[] serialize(BidReceipt data);
	public abstract byte[] serialize(BiddingOver data);
	public abstract byte[] serialize(Concession data);
	public abstract byte[] serialize(BidConveyData data);
	public abstract byte[] serialize(SelloffEnd data);
	
	public byte[] serialize(SelloffMessageData msg) throws NotSerializableException {
		if (msg instanceof SelloffStart) {
			return serialize((SelloffStart)msg);
		}else if (msg instanceof PromiseData){
			return serialize((PromiseData)msg);
		}else if (msg instanceof BidReceipt){
			return serialize((BidReceipt)msg);
		}else if (msg instanceof OfferAnalysisData){
			return serialize((OfferAnalysisData)msg);
		}else if (msg instanceof BiddingOver){
			return serialize((BiddingOver)msg);
		}else if (msg instanceof Concession){
			return serialize((Concession)msg);
		}else if (msg instanceof BidConveyData){
			return serialize((BidConveyData)msg);
		}else if (msg instanceof SelloffEnd){
			return serialize((SelloffEnd)msg);
		}else throw new NotSerializableException("BidPalSerializer received BidPalMessageData of unsupported type " + msg.type);
	}
	
	public abstract SelloffMessageData deserialize(byte[] msg) throws IOException;
}

