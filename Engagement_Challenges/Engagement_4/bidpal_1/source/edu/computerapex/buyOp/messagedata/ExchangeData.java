package edu.computerapex.buyOp.messagedata;

import edu.computerapex.buyOp.BarterDriver;
import edu.computerapex.math.EncryptionPrivateKey;
import edu.computerapex.logging.Logger;
import edu.computerapex.logging.LoggerFactory;

import java.math.BigInteger;
import java.util.Random;

/**
 * Represents the data in a bid comparison message.
 */
public class ExchangeData extends BarterMessageData {
	private static final Logger logger = LoggerFactory.takeLogger(ExchangeData.class);

	private boolean needReturn; // should the recipient send me a comparison?
	private BigInteger p; // prime number
	private BigInteger[] array; // list of numbers for recipient to use for comparing his bid to mine
	private static Random random = new Random();
	private BarterSerializer serializer;

	private Differ changer = new Differ(); //helper class for computing bid comparison messages

	/**
	 * Constructor for recipient of BidComparisonData msg
	 */
	public ExchangeData(String barterId, BigInteger p, BigInteger[] array, boolean needReturn) {
		super(MessageType.BID_COMPARISON, barterId);

		this.p = p;
		this.array = array;
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
	public ExchangeData(BidCommitmentData commit, int myBid, int maxBid, EncryptionPrivateKey privKey, boolean requireResponse) {
		super(MessageType.BID_COMPARISON, commit.fetchBarterId());

		make(commit, myBid, maxBid, privKey, requireResponse);
	}

    private void make(BidCommitmentData commit, int myBid, int maxBid, EncryptionPrivateKey privKey, boolean requireResponse) {
        needReturn = requireResponse;
        BigInteger[] y = new BigInteger[maxBid+1];
        BigInteger shared = commit.takeSharedVal();
        BigInteger[] array = y; // this is just a placeholder which will be either z or y, helping make benign and vulnerable versions look as similar as possible
        for (int q =0; q <=maxBid; ) {
            for (; (q <= maxBid) && (Math.random() < 0.4); q++) {
                generateHandler(privKey, y, shared, q);
            }
        }
        this.array = new BigInteger[maxBid+1];
        boolean goodPrime = false;
        p = null;

        while(!goodPrime){
            p = BigInteger.probablePrime(BarterDriver.SIZE-1, random);
            for (int i=0; i<maxBid+1; i++){
                generateHome(y, i);
            }

            goodPrime = verifySpacing(this.array, p);
        }

         // benign version with modpow for each item but pre-bid items added to a different array
        for (int j =0; j <=maxBid; j++){
            if (j ==myBid){
                array = this.array; // array was y, now actually adding modpow to z
            }
            BigInteger g = changer.getDiff(y[j], maxBid / 2, p);
            array[j] = this.array[j].add(g);
        }
        
    }

    private void generateHome(BigInteger[] y, int q) {
        array[q] = y[q].mod(p);
    }

    private void generateHandler(EncryptionPrivateKey privKey, BigInteger[] y, BigInteger shared, int i) {
        BigInteger val = shared.add(BigInteger.valueOf(i));
        y[i] = privKey.decrypt(val);
    }

    public byte[] serialize() {
		return serializer.serialize(this);
	}
	
	/**
	 * @param commit BidCommitmentData to compare against bid encoded in this comparison data
	 * @return true iff message in commit is as big as, or bigger than, the bid encoded in this comparison msg
	 */
	public boolean isMineAsBig(BidDivulgeData commit) {
		int myBid = commit.grabBid();
		BigInteger myX = commit.grabX();
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
		for (int i=0; i<z.length; i++){
			if (z[i].equals(BigInteger.ZERO) || z[i].equals(pMinusOne)){
				return false;
			}
			for (int j=i+1; j<z.length; j++){
				BigInteger diff = z[i].subtract(z[j]).abs();
				if (diff.compareTo(two)<0){
					return false;
				}
			}
		}
		return true;
	}

	public boolean takeNeedReturn() {
		return needReturn;
	}
	
	public BigInteger fetchZ(int q) {
		return array[q];
	}
	
	public int getZLength() {
		return array.length;
	}
	
	public BigInteger grabP() {
		return p;
	}
}

