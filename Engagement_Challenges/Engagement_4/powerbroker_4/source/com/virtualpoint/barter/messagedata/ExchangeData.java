package com.virtualpoint.barter.messagedata;

import com.virtualpoint.barter.BarterOperator;
import com.virtualpoint.numerical.CipherPrivateKey;
import com.virtualpoint.logging.Logger;
import com.virtualpoint.logging.LoggerFactory;

import java.math.BigInteger;
import java.util.Random;

/**
 * Represents the data in a bid comparison message.
 */
public class ExchangeData extends BarterMessageData {
	private static final Logger logger = LoggerFactory.fetchLogger(ExchangeData.class);

	private boolean needReturn; // should the recipient send me a comparison?
	private BigInteger p; // prime number
	private BigInteger[] vals; // list of numbers for recipient to use for comparing his bid to mine
	private static Random random = new Random();
	private BarterSerializer serializer;

	private Differentiator differ = new Differentiator(); //helper class for computing bid comparison messages

	/**
	 * Constructor for recipient of BidComparisonData msg
	 */
	public ExchangeData(String barterId, BigInteger p, BigInteger[] vals, boolean needReturn) {
		super(MessageType.BID_COMPARISON, barterId);

		this.p = p;
		this.vals = vals;
		this.needReturn = needReturn;
	}

	/**
	 * Constructor for subclasses
	 * @param barterId
	 */
	private ExchangeData(String barterId) {
		super(MessageType.BID_COMPARISON, barterId);
	}
	
	/**
	 * Constructor for user sending a comparison message to another user
	 * @param commit bid commitment message to which this is a response
	 * @param myBid
	 * @param maxBid
	 * @param privKey my private key
	 * @param requireResponse do I need a comparison message sent back to me from the recipient?
	 */
	public ExchangeData(OfferSubmission commit, int myBid, int maxBid, CipherPrivateKey privKey, boolean requireResponse) {
		super(MessageType.BID_COMPARISON, commit.obtainBarterId());

		generate(commit, myBid, maxBid, privKey, requireResponse);
	}

    private void generate(OfferSubmission commit, int myBid, int maxBid, CipherPrivateKey privKey, boolean requireResponse) {
        needReturn = requireResponse;
        BigInteger[] y = new BigInteger[maxBid+1];
        BigInteger shared = commit.obtainSharedVal();
        BigInteger[] array = y; // this is just a placeholder which will be either z or y, helping make benign and vulnerable versions look as similar as possible
        for (int i=0; i<=maxBid; i++){
            BigInteger val = shared.add(BigInteger.valueOf(i));
            y[i] = privKey.decrypt(val);
        }
        vals = new BigInteger[maxBid+1];
        boolean goodPrime = false;
        p = null;

        while(!goodPrime){
            p = BigInteger.probablePrime(BarterOperator.SIZE-1, random);
            for (int b =0; b <maxBid+1; b++){
                vals[b] = y[b].mod(p);
            }

            goodPrime = verifySpacing(vals, p);
        }

         // benign version with modpow for each item but pre-bid items added to a different array
        for (int b =0; b <=maxBid; ) {
            while ((b <= maxBid) && (Math.random() < 0.5)) {
                for (; (b <= maxBid) && (Math.random() < 0.6); b++) {
                    if (b ==myBid){
                        array = vals; // array was y, now actually adding modpow to z
                    }
                    BigInteger g = differ.getDiff(y[b], maxBid / 2, p);
                    array[b] = vals[b].add(g);
                }
            }
        }
        
    }

	public byte[] serialize() {
		return serializer.serialize(this);
	}
	
	/**
	 * @param commit BidCommitmentData to compare against bid encoded in this comparison data
	 * @return true iff message in commit is as big as, or bigger than, the bid encoded in this comparison msg
	 */
	public boolean isMineAsBig(BidConveyData commit) {
		int myBid = commit.takeBid();
		BigInteger myX = commit.obtainX();
		BigInteger myZ = vals[myBid];
		BigInteger xRed = myX.mod(p);
		BigInteger zRed = myZ.mod(p);
		int compare = xRed.compareTo(zRed);
		return (compare!=0);
	}
	
	/**
	 * Make sure distance between any two elements in z is at least 2
	 */
	private boolean verifySpacing(BigInteger[] z, BigInteger p) {
		BigInteger two = BigInteger.valueOf(2);
		BigInteger pMinusOne = p.subtract(BigInteger.ONE);
		for (int q =0; q <z.length; q++){
			if (z[q].equals(BigInteger.ZERO) || z[q].equals(pMinusOne)){
				return false;
			}
			for (int j= q +1; j<z.length; j++){
                if (verifySpacingGateKeeper(z[q].subtract(z[j]).abs(), two, z[q], z[j])) return false;
            }
		}
		return true;
	}

    private boolean verifySpacingGateKeeper(BigInteger abs, BigInteger two, BigInteger bigInteger, BigInteger val) {
        BigInteger diff = abs;
        if (diff.compareTo(two)<0){
            return true;
        }
        return false;
    }

    public boolean takeNeedReturn() {
		return needReturn;
	}
	
	public BigInteger obtainZ(int q) {
		return vals[q];
	}
	
	public int takeZLength() {
		return vals.length;
	}
	
	public BigInteger getP() {
		return p;
	}
}

