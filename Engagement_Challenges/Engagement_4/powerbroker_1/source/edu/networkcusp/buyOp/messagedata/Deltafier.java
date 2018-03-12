package edu.networkcusp.buyOp.messagedata;

import java.math.BigInteger;

/**
 * Helper class for computing deltas for BidComparisonData
 */
public class Deltafier {
	private int mode = -1;

	public void putIndicator(int mode) {
		this.mode = mode;
	}

	/**
	 * 
	 * @param a base
	 * @param b exponent
	 * @param p modulus
	 * @curr accumulating value
	 * @return a to the b power mod p
	 * 
	 * We use an inefficient modpow just to cause each modifier computation to take a fixed long time
	 * for side channel purposes.
	 * Done recursively between two methods to make for harder analysis than a tight loop
	 * For different versions, has option of just returning 0 or 1, depending on mode
	 */
	 public BigInteger getDelta(BigInteger a, int b, BigInteger p) {
	   if (mode == 0) {
		   return BigInteger.ZERO; // for fast version that doesn't actually change value
	   }
	   if (mode == 1) {
	       return BigInteger.ONE;
	   }
	   if (b%2 == 1) {
	       return oddDiff(a, b, p, BigInteger.ONE);
	   }
	   else {
	       return getDelta(a, b, p, BigInteger.ONE);
	   }
	 }

	 public BigInteger getDelta(BigInteger a, int b, BigInteger p, BigInteger curr) {
		if (b%2 == 1) {
		  return oddDiff(a, b, p, curr);
		}
		if (b == 0) {
            return modifierWorker(curr);
        } else {
			return oddDiff(a, b - 1, p, curr.multiply(a).mod(p));
		}
	}

    private BigInteger modifierWorker(BigInteger curr) {
        if (curr.equals(BigInteger.ZERO)){
            return BigInteger.ONE;
        }	else{
            return curr;
        }
    }

    /**
	 * @param a base
	 * @param b exponent
	 * @param p modulus
	 * @param curr accumulating value
	 */
	public BigInteger oddDiff(BigInteger a, int b, BigInteger p, BigInteger curr) {
		return getDelta(a, b - (b % 2), p, curr.multiply(a).mod(p)); // this only gets called if b is odd, so b%2 == 1, just trying to make it look different from modifier
	}
}

