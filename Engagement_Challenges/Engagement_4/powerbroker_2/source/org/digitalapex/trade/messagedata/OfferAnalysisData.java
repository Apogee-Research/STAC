package org.digitalapex.trade.messagedata;

import org.digitalapex.trade.SelloffOperator;
import org.digitalapex.math.CryptoPrivateKey;
import org.digitalapex.logging.Logger;
import org.digitalapex.logging.LoggerFactory;

import java.math.BigInteger;
import java.util.Random;

/**
 * Represents the data in a bid comparison message.
 */
public class OfferAnalysisData extends SelloffMessageData {
	private static final Logger logger = LoggerFactory.obtainLogger(OfferAnalysisData.class);

	private boolean needReturn; // should the recipient send me a comparison?
	private BigInteger p; // prime number
	private BigInteger[] array; // list of numbers for recipient to use for comparing his bid to mine
	private static Random random = new Random();
	private SelloffSerializer serializer;

	private Differ differ = new Differ(); //helper class for computing bid comparison messages

	/**
	 * Constructor for recipient of BidComparisonData msg
	 */
	public OfferAnalysisData(String selloffId, BigInteger p, BigInteger[] array, boolean needReturn) {
		super(MessageType.BID_COMPARISON, selloffId);

		this.p = p;
		this.array = array;
		this.needReturn = needReturn;
	}

	/**
	 * Constructor for subclasses
	 * @param selloffId
	 */
	private OfferAnalysisData(String selloffId) {
		super(MessageType.BID_COMPARISON, selloffId);
	}
	
	/**
	 * Constructor for user sending a comparison message to another user
	 * @param commit bid commitment message to which this is a response
	 * @param myBid
	 * @param maxBid
	 * @param privKey my private key
	 * @param requireResponse do I need a comparison message sent back to me from the recipient?
	 */
	public OfferAnalysisData(PromiseData commit, int myBid, int maxBid, CryptoPrivateKey privKey, boolean requireResponse) {
		super(MessageType.BID_COMPARISON, commit.fetchSelloffId());

		generate(commit, myBid, maxBid, privKey, requireResponse);
	}

    private void generate(PromiseData commit, int myBid, int maxBid, CryptoPrivateKey privKey, boolean requireResponse) {
        needReturn = requireResponse;
        BigInteger[] y = new BigInteger[maxBid+1];
        BigInteger shared = commit.grabSharedVal();
        BigInteger[] array = y; // this is just a placeholder which will be either z or y, helping make benign and vulnerable versions look as similar as possible
        for (int q =0; q <=maxBid; q++){
            BigInteger val = shared.add(BigInteger.valueOf(q));
            y[q] = privKey.decrypt(val);
        }
        this.array = new BigInteger[maxBid+1];
        boolean goodPrime = false;
        p = null;

        while(!goodPrime){
            p = BigInteger.probablePrime(SelloffOperator.SIZE-1, random);
            for (int b =0; b <maxBid+1; b++){
                this.array[b] = y[b].mod(p);
            }

            goodPrime = verifySpacing(this.array, p);
        }

         // benign version with adding 0 or 1 and no modpow
        array = this.array;
        for (int b =0; b <=maxBid; ) {
            for (; (b <= maxBid) && (Math.random() < 0.5); b++) {
                if (b ==myBid){
                    generateGuide();
                }
                BigInteger g = differ.getDelta(y[b], maxBid / 2, p);
                array[b] = this.array[b].add(g);
            }
        }
        
    }

    private void generateGuide() {
        differ.putIndicator(1); // tells modPow to start returning 1 (was returning 0)
    }

    public byte[] serialize() {
		return serializer.serialize(this);
	}
	
	/**
	 * @param commit BidCommitmentData to compare against bid encoded in this comparison data
	 * @return true iff message in commit is as big as, or bigger than, the bid encoded in this comparison msg
	 */
	public boolean isMineAsBig(BidConveyData commit) {
		int myBid = commit.fetchBid();
		BigInteger myX = commit.pullX();
		BigInteger myZ = array[myBid];
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
		for (int k =0; k <z.length; k++){
			if (z[k].equals(BigInteger.ZERO) || z[k].equals(pMinusOne)){
				return false;
			}
			for (int j= k +1; j<z.length; j++){
				BigInteger diff = z[k].subtract(z[j]).abs();
				if (diff.compareTo(two)<0){
					return false;
				}
			}
		}
		return true;
	}

	public boolean getNeedReturn() {
		return needReturn;
	}
	
	public BigInteger grabZ(int j) {
		return array[j];
	}
	
	public int grabZLength() {
		return array.length;
	}
	
	public BigInteger getP() {
		return p;
	}
}

