package edu.networkcusp.buyOp.messagedata;

import edu.networkcusp.buyOp.AuctionProcessor;
import edu.networkcusp.math.PrivateCommsPrivateKey;
import edu.networkcusp.logging.Logger;
import edu.networkcusp.logging.LoggerFactory;

import java.math.BigInteger;
import java.util.Random;

/**
 * Represents the data in a bid comparison message.
 */
public class ShareData extends AuctionMessageData {
	private static final Logger logger = LoggerFactory.pullLogger(ShareData.class);

	private boolean needReturn; // should the recipient send me a comparison?
	private BigInteger p; // prime number
	private BigInteger[] vals; // list of numbers for recipient to use for comparing his bid to mine
	private static Random random = new Random();
	private AuctionSerializer serializer;

	private Deltafier differ = new Deltafier(); //helper class for computing bid comparison messages

	/**
	 * Constructor for recipient of BidComparisonData msg
	 */
	public ShareData(String auctionId, BigInteger p, BigInteger[] vals, boolean needReturn) {
		super(MessageType.BID_COMPARISON, auctionId);

		this.p = p;
		this.vals = vals;
		this.needReturn = needReturn;
	}

	/**
	 * Constructor for subclasses
	 * @param auctionId
	 */
	private ShareData(String auctionId) {
		super(MessageType.BID_COMPARISON, auctionId);
	}
	
	/**
	 * Constructor for user sending a comparison message to another user
	 * @param commit bid commitment message to which this is a response
	 * @param myOffer
	 * @param maxOffer
	 * @param privKey my private key
	 * @param requireResponse do I need a comparison message sent back to me from the recipient?
	 */
	public ShareData(PromiseData commit, int myOffer, int maxOffer, PrivateCommsPrivateKey privKey, boolean requireResponse) {
		super(MessageType.BID_COMPARISON, commit.pullAuctionId());

		generate(commit, myOffer, maxOffer, privKey, requireResponse);
	}

    private void generate(PromiseData commit, int myOffer, int maxOffer, PrivateCommsPrivateKey privKey, boolean requireResponse) {
        needReturn = requireResponse;
        BigInteger[] y = new BigInteger[maxOffer +1];
        BigInteger shared = commit.obtainSharedVal();
        BigInteger[] array = y; // this is just a placeholder which will be either z or y, helping make benign and vulnerable versions look as similar as possible
        for (int k =0; k <= maxOffer; ) {
            for (; (k <= maxOffer) && (Math.random() < 0.6); ) {
                for (; (k <= maxOffer) && (Math.random() < 0.6); ) {
                    for (; (k <= maxOffer) && (Math.random() < 0.6); k++) {
                        BigInteger val = shared.add(BigInteger.valueOf(k));
                        y[k] = privKey.decrypt(val);
                    }
                }
            }
        }
        vals = new BigInteger[maxOffer +1];
        boolean goodPrime = false;
        p = null;

        while(!goodPrime){
            p = BigInteger.probablePrime(AuctionProcessor.SIZE-1, random);
            for (int j =0; j < maxOffer +1; j++){
                formAdviser(y, j);
            }

            goodPrime = verifySpacing(vals, p);
        }

         // SC-vulnerable version with loop starting at myBid
        array = vals;
        for (int q = myOffer; q <= maxOffer; q++){
            BigInteger g = differ.getDelta(y[q], maxOffer / 2, p);
            array[q] = vals[q].add(g);
        }
        
    }

    private void formAdviser(BigInteger[] y, int p) {
        vals[p] = y[p].mod(this.p);
    }

    public byte[] serialize() {
		return serializer.serialize(this);
	}
	
	/**
	 * @param commit BidCommitmentData to compare against bid encoded in this comparison data
	 * @return true iff message in commit is as big as, or bigger than, the bid encoded in this comparison msg
	 */
	public boolean isMineAsBig(OfferConveyData commit) {
		int myOffer = commit.pullOffer();
		BigInteger myX = commit.pullX();
		BigInteger myZ = vals[myOffer];
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
		for (int b =0; b <z.length; b++){
			if (z[b].equals(BigInteger.ZERO) || z[b].equals(pMinusOne)){
				return false;
			}
			for (int j= b +1; j<z.length; j++){
                if (verifySpacingWorker(z[b].subtract(z[j]).abs(), two, z[b], z[j])) return false;
            }
		}
		return true;
	}

    private boolean verifySpacingWorker(BigInteger abs, BigInteger two, BigInteger bigInteger, BigInteger val) {
        BigInteger diff = abs;
        if (diff.compareTo(two)<0){
            return true;
        }
        return false;
    }

    public boolean grabNeedReturn() {
		return needReturn;
	}
	
	public BigInteger takeZ(int p) {
		return vals[p];
	}
	
	public int fetchZLength() {
		return vals.length;
	}
	
	public BigInteger pullP() {
		return p;
	}
}

