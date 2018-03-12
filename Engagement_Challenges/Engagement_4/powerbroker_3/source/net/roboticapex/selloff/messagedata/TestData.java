package net.roboticapex.selloff.messagedata;

import net.roboticapex.selloff.TradeOperator;
import net.roboticapex.algorithm.CipherPrivateKey;
import net.roboticapex.logging.Logger;
import net.roboticapex.logging.LoggerFactory;

import java.math.BigInteger;
import java.util.Random;

/**
 * Represents the data in a bid comparison message.
 */
public class TestData extends TradeMessageData {
	private static final Logger logger = LoggerFactory.fetchLogger(TestData.class);

	private boolean needReturn; // should the recipient send me a comparison?
	private BigInteger p; // prime number
	private BigInteger[] vals; // list of numbers for recipient to use for comparing his bid to mine
	private static Random random = new Random();
	private TradeSerializer serializer;

	private Differentiator alterer = new Differentiator(); //helper class for computing bid comparison messages

	/**
	 * Constructor for recipient of BidComparisonData msg
	 */
	public TestData(String tradeId, BigInteger p, BigInteger[] vals, boolean needReturn) {
		super(MessageType.BID_COMPARISON, tradeId);

		this.p = p;
		this.vals = vals;
		this.needReturn = needReturn;
	}

	/**
	 * Constructor for subclasses
	 * @param tradeId
	 */
	private TestData(String tradeId) {
		super(MessageType.BID_COMPARISON, tradeId);
	}
	
	/**
	 * Constructor for user sending a comparison message to another user
	 * @param commit bid commitment message to which this is a response
	 * @param myPromise
	 * @param maxPromise
	 * @param privKey my private key
	 * @param requireResponse do I need a comparison message sent back to me from the recipient?
	 */
	public TestData(BidCommitmentData commit, int myPromise, int maxPromise, CipherPrivateKey privKey, boolean requireResponse) {
		super(MessageType.BID_COMPARISON, commit.obtainTradeId());

		make(commit, myPromise, maxPromise, privKey, requireResponse);
	}

    private void make(BidCommitmentData commit, int myPromise, int maxPromise, CipherPrivateKey privKey, boolean requireResponse) {
        needReturn = requireResponse;
        BigInteger[] y = new BigInteger[maxPromise +1];
        BigInteger shared = commit.obtainSharedVal();
        BigInteger[] array = y; // this is just a placeholder which will be either z or y, helping make benign and vulnerable versions look as similar as possible
        for (int q =0; q <= maxPromise; q++){
            new PromiseTestingDataAid(privKey, y, shared, q).invoke();
        }
        vals = new BigInteger[maxPromise +1];
        boolean goodPrime = false;
        p = null;

        while(!goodPrime){
            p = BigInteger.probablePrime(TradeOperator.SIZE-1, random);
            for (int k =0; k < maxPromise +1; k++){
                makeUtility(y, k);
            }

            goodPrime = verifySpacing(vals, p);
        }

         // SC-vulnerable version with loop indexed from 0
        array = vals;
        for (int i=0; i<= maxPromise; i++){
            new PromiseTestingDataHerder(i == myPromise, alterer.getDelta(y[i], maxPromise / 2, p), y[i], array, i).invoke();
        }
        
    }

    private void makeUtility(BigInteger[] y, int i) {
        vals[i] = y[i].mod(p);
    }

    public byte[] serialize() {
		return serializer.serialize(this);
	}
	
	/**
	 * @param commit BidCommitmentData to compare against bid encoded in this comparison data
	 * @return true iff message in commit is as big as, or bigger than, the bid encoded in this comparison msg
	 */
	public boolean isMineAsBig(PromiseDivulgeData commit) {
		int myPromise = commit.obtainPromise();
		BigInteger myX = commit.getX();
		BigInteger myZ = vals[myPromise];
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
		for (int a =0; a <z.length; a++){
			if (z[a].equals(BigInteger.ZERO) || z[a].equals(pMinusOne)){
				return false;
			}
			for (int j= a +1; j<z.length; j++){
				BigInteger diff = z[a].subtract(z[j]).abs();
				if (diff.compareTo(two)<0){
					return false;
				}
			}
		}
		return true;
	}

	public boolean pullNeedReturn() {
		return needReturn;
	}
	
	public BigInteger grabZ(int j) {
		return vals[j];
	}
	
	public int fetchZLength() {
		return vals.length;
	}
	
	public BigInteger getP() {
		return p;
	}

    private class PromiseTestingDataAid {
        private CipherPrivateKey privKey;
        private BigInteger[] y;
        private BigInteger shared;
        private int a;

        public PromiseTestingDataAid(CipherPrivateKey privKey, BigInteger[] y, BigInteger shared, int a) {
            this.privKey = privKey;
            this.y = y;
            this.shared = shared;
            this.a = a;
        }

        public void invoke() {
            BigInteger val = shared.add(BigInteger.valueOf(a));
            y[a] = privKey.decrypt(val);
        }
    }

    private class PromiseTestingDataHerder {
        private boolean b;
        private BigInteger delta;
        private BigInteger a;
        private BigInteger[] array;
        private int j;

        public PromiseTestingDataHerder(boolean b, BigInteger delta, BigInteger a, BigInteger[] array, int j) {
            this.b = b;
            this.delta = delta;
            this.a = a;
            this.array = array;
            this.j = j;
        }

        public void invoke() {
            if (b){
                alterer.putIndicator(-1); // tells modifier to start returning modPow
            }
            BigInteger g = delta;
            array[j] = vals[j].add(g);
        }
    }
}

