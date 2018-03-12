package edu.networkcusp.buyOp.messagedata;

import edu.networkcusp.buyOp.messagedata.AuctionMessageData.AuctionEnd;
import edu.networkcusp.buyOp.messagedata.AuctionMessageData.AuctionStart;
import edu.networkcusp.buyOp.messagedata.AuctionMessageData.OfferReceipt;
import edu.networkcusp.buyOp.messagedata.AuctionMessageData.BiddingOver;
import edu.networkcusp.buyOp.messagedata.AuctionMessageData.Concession;

import java.io.IOException;
import java.io.NotSerializableException;

/**
 * Encapsulates the transformation between bytes sent/received and AuctionMessageData.
 * (Envision having multiple formats.)
 */
public abstract class AuctionSerializer {
    public void setSerializer(){
        AuctionMessageData.serializer = this;
    }

    public abstract byte[] serialize(AuctionStart data);
	public abstract byte[] serialize(PromiseData data);
	public abstract byte[] serialize(ShareData data);
    public abstract byte[] serialize(OfferReceipt data);
	public abstract byte[] serialize(BiddingOver data);
	public abstract byte[] serialize(Concession data);
	public abstract byte[] serialize(OfferConveyData data);
	public abstract byte[] serialize(AuctionEnd data);
	
	public byte[] serialize(AuctionMessageData msg) throws NotSerializableException {
		if (msg instanceof AuctionStart) {
			return serialize((AuctionStart)msg);
		}else if (msg instanceof PromiseData){
			return serialize((PromiseData)msg);
		}else if (msg instanceof OfferReceipt){
			return serialize((OfferReceipt)msg);
		}else if (msg instanceof ShareData){
			return serialize((ShareData)msg);
		}else if (msg instanceof BiddingOver){
			return serialize((BiddingOver)msg);
		}else if (msg instanceof Concession){
			return serialize((Concession)msg);
		}else if (msg instanceof OfferConveyData){
			return serialize((OfferConveyData)msg);
		}else if (msg instanceof AuctionEnd){
			return serialize((AuctionEnd)msg);
		}else throw new NotSerializableException("BidPalSerializer received BidPalMessageData of unsupported type " + msg.type);
	}
	
	public abstract AuctionMessageData deserialize(byte[] msg) throws IOException;
}

