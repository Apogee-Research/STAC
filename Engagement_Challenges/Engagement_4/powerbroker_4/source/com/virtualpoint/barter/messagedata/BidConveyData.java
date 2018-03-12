package com.virtualpoint.barter.messagedata;

import com.virtualpoint.talkers.DialogsPublicIdentity;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class BidConveyData extends BarterMessageData {
	private int bid;
	private double r1;
    private double r2;
	private BigInteger x;
	private byte[] hash;  // H(r1, r2, bid, x)
	
	static Random random = new Random();
	
	/**
	 * Constructor for a received  bid reveal message
	 * @param revealMsg
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public BidConveyData(String barterId, int bid, BigInteger x, double r1, double r2) throws IOException, NoSuchAlgorithmException{
		super(MessageType.CLAIM_WIN, barterId);
		this.bid = bid;
		this.x = x;
		this.r1 = r1;
		this.r2 = r2;
		hash = makeHash();
	}
	
	/**
	 * Constructor for the user making the bid
	 * @param bid
	 * @param size
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public BidConveyData(String barterId, int bid, int size) throws IOException, NoSuchAlgorithmException{
		super(MessageType.CLAIM_WIN, barterId);
		this.bid = bid;
		x = new BigInteger(size, random);
        r1 = random.nextDouble();
        r2 = random.nextDouble();
        hash = makeHash();
	}
	
	
	 /** Get message for commiting our bid
	 * @param format PROTOBUF or other MessageFormat
	 * @param dest identity to which commit is being sent
	 * @return
	 */
	public OfferSubmission fetchContractData(DialogsPublicIdentity dest){
		BigInteger share = dest.getPublicKey().encrypt(x).subtract(BigInteger.valueOf(bid));
		OfferSubmission commit = new OfferSubmission(obtainBarterId(), hash, r1, share);
		return commit;
		
	}
	
	/**
     * create hash of stuff for commitment
     * @return hash  H(r1, r2, bid, x)
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private byte[] makeHash() throws IOException, NoSuchAlgorithmException{
    	
    	MessageDigest md = MessageDigest.getInstance("SHA-256");
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	DataOutputStream dos = new DataOutputStream(bos);
    	dos.writeDouble(r1);
    	dos.writeDouble(r2);
    	dos.writeInt(bid);
    	byte[] xBytes = x.toByteArray();
    	dos.write(xBytes, 0, xBytes.length);
    	dos.flush();
    	byte[] hash = md.digest(bos.toByteArray());
    	return hash;    
	}
    
    public byte[] getHash(){
    	return hash;
    }
    
    public int takeBid(){
    	return bid;
    }
    
    public BigInteger obtainX(){
    	return x;
    }
    
    public double fetchR1(){
    	return r1;
    }
    
    public double getR2(){
    	return r2;
    }
}
