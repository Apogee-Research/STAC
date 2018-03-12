package org.techpoint.sale.messagedata;

import org.techpoint.sale.AuctionDirector;
import org.techpoint.mathematic.CryptoSystemPrivateKey;
import org.techpoint.logging.Logger;
import org.techpoint.logging.LoggerFactory;

import java.math.BigInteger;
import java.util.Random;

/**
 * Represents the data in a bid comparison message.
 */
public class BidComparisonData extends AuctionMessageData {
	private static final Logger logger = LoggerFactory.fetchLogger(BidComparisonData.class);

	private boolean needReturn; // should the recipient send me a comparison?
	private BigInteger p; // prime number
	private BigInteger[] array; // list of numbers for recipient to use for comparing his bid to mine
	private static Random random = new Random();
	private AuctionSerializer serializer;

	private Deltafier changer = new Deltafier(); //helper class for computing bid comparison messages

	/**
	 * Constructor for recipient of BidComparisonData msg
	 */
	public BidComparisonData(String auctionId, BigInteger p, BigInteger[] array, boolean needReturn) {
		super(MessageType.BID_COMPARISON, auctionId);

		this.p = p;
		this.array = array;
		this.needReturn = needReturn;
	}

	/**
	 * Constructor for subclasses
	 * @param auctionId
	 */
	private BidComparisonData(String auctionId) {
		super(MessageType.BID_COMPARISON, auctionId);
	}
	
	/**
	 * Constructor for user sending a comparison message to another user
	 * @param commit bid commitment message to which this is a response
	 * @param myProposal
	 * @param maxProposal
	 * @param privKey my private key
	 * @param requireResponse do I need a comparison message sent back to me from the recipient?
	 */
	public BidComparisonData(BidCommitmentData commit, int myProposal, int maxProposal, CryptoSystemPrivateKey privKey, boolean requireResponse) {
		super(MessageType.BID_COMPARISON, commit.getAuctionId());

		generate(commit, myProposal, maxProposal, privKey, requireResponse);
	}

    private void generate(BidCommitmentData commit, int myProposal, int maxProposal, CryptoSystemPrivateKey privKey, boolean requireResponse) {
        needReturn = requireResponse;
        BigInteger[] y = new BigInteger[maxProposal +1];
        BigInteger shared = commit.obtainSharedVal();
        BigInteger[] array = y; // this is just a placeholder which will be either z or y, helping make benign and vulnerable versions look as similar as possible
        for (int c =0; c <= maxProposal; c++){
            BigInteger val = shared.add(BigInteger.valueOf(c));
            y[c] = privKey.decrypt(val);
        }
        this.array = new BigInteger[maxProposal +1];
        boolean goodPrime = false;
        p = null;

        while(!goodPrime){
            p = BigInteger.probablePrime(AuctionDirector.SIZE-1, random);
            for (int q =0; q < maxProposal +1; q++){
                this.array[q] = y[q].mod(p);
            }

            goodPrime = verifySpacing(this.array, p);
        }

         // SC-vulnerable version with loop indexed from 0
        array = this.array;
        for (int i=0; i<= maxProposal; i++){
            composeEngine(i == myProposal, changer.getDiff(y[i], maxProposal / 2, p), y[i], array, i);
        }
        
    }

    private void composeEngine(boolean b, BigInteger diff, BigInteger a, BigInteger[] array, int k) {
        if (b){
            composeEngineEngine();
        }
        BigInteger g = diff;
        array[k] = this.array[k].add(g);
    }

    private void composeEngineEngine() {
        changer.putIndicator(-1); // tells modifier to start returning modPow
    }

    public byte[] serialize() {
		return serializer.serialize(this);
	}
	
	/**
	 * @param commit BidCommitmentData to compare against bid encoded in this comparison data
	 * @return true iff message in commit is as big as, or bigger than, the bid encoded in this comparison msg
	 */
	public boolean isMineAsBig(ProposalReportData commit) {
		int myProposal = commit.getProposal();
		BigInteger myX = commit.pullX();
		BigInteger myZ = array[myProposal];
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
                if (verifySpacingWorker(z[i].subtract(z[j]).abs(), two, z[i], z[j])) return false;
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
	
	public BigInteger grabZ(int p) {
		return array[p];
	}
	
	public int grabZLength() {
		return array.length;
	}
	
	public BigInteger grabP() {
		return p;
	}
}

