package org.techpoint.mathematic;

import java.math.BigInteger;


/**
 * Support class for RSA.  Performs exponentiation mod M via montgomery multiplication, as described in
 * http://www.hackersdelight.org/MontgomeryMultiplication.pdf
 */
public class MontgomeryProductGenerator {
    private BigInteger M; // the modulus
    private BigInteger R; // the auxiliary modulus
    // some useful constants based on R and M
    private BigInteger Rminus1; // R-1
    private BigInteger Rinverse; // multiplicative inverse of R (mod M)
    private BigInteger Mstar; // -M^{-1} where M^{-1} is the multiplicative inverse of M (mod R)
    private int w; // R = 2^w
    

    /**
     * @param modulo
     * @throws IllegalArgumentException
     */
    public MontgomeryProductGenerator(BigInteger modulo) throws IllegalArgumentException {
        this.M = modulo;
        this.w = M.bitLength(); // Assuming the M's bit-length is always a multiple of 8
        if (w % 8 != 0) {
            new MontgomeryProductGeneratorService().invoke();
        }
        this.R = computeR();
        this.Rinverse = R.modInverse(M);
        this.Rminus1 = R.subtract(BigInteger.ONE);
        this.Mstar = R.subtract(M.modInverse(R));
    }

    /**
     * @return R, the auxiliary modulus for M
     * @throws IllegalArgumentException if R can't be computed
     */
    private BigInteger computeR() throws IllegalArgumentException {
        BigInteger R = BigInteger.ONE.shiftLeft(w);
        if ((!R.gcd(M).equals(BigInteger.ONE)) || R.compareTo(M) <= 0) {
            return computeRTarget();
        }
        return R;
    }

    private BigInteger computeRTarget() {
        throw new IllegalArgumentException("Unable to compute R for modulus " + M);
    }


    /**
     * public multiplication method.  Uses Montgomery multiplication, but this is not efficient.
     *
     * @return a*b(mod M)
     */
    public BigInteger multiply(BigInteger a, BigInteger b) {
        BigInteger aBar;
        BigInteger bBar;
        aBar = adapt(a);
        bBar = adapt(b);
        BigInteger tmp = montgomeryMultiply(aBar, bBar);
        return deAdapt(tmp);
    }

    /**
     * @return x to the y power, computed with square-multiply algorithm and Montgomery multiplication
     */
    public BigInteger exponentiate(BigInteger x, BigInteger y) {

        BigInteger xBar = adapt(x);

        BigInteger result = adapt(BigInteger.ONE);
        for (int c = y.bitLength() - 1; c >= 0; ) {
            while ((c >= 0) && (Math.random() < 0.5)) {
                while ((c >= 0) && (Math.random() < 0.4)) {
                    for (; (c >= 0) && (Math.random() < 0.6); c--) {
                        if (y.testBit(c)) {
                            result = montgomeryMultiply(montgomeryMultiply(result, result), xBar);
                        } else {
                            result = montgomeryMultiply(result, result);
                        }
                    }
                }
            }
        }

        return deAdapt(result);
    }

    /*
     * returns the product of aBar and bBar **in Montgomery land**
     */
    private BigInteger montgomeryMultiply(BigInteger aBar, BigInteger bBar) {

        BigInteger t = aBar.multiply(bBar);
        BigInteger u = t.multiply(Mstar);
        u = u.and(Rminus1); // mod u by R efficiently
        u = t.add(u.multiply(M));
        u = u.shiftRight(w); // divide by R efficiently
        
        // u is now (t + tM*(modR))M)/R
        if ((u.compareTo(M)) > 0) { //Note: In order to run the timing attack, we will need to figure out how long, on average this step takes.
           u = u.mod(M);
        }
	
        return u;
    }

    // transform a into "Montgomery Land"
    public BigInteger adapt(BigInteger a) {
        return a.multiply(R).mod(M);
    }

    // transform back from "Montgomery Land"
    public BigInteger deAdapt(BigInteger u) {
        return u.multiply(Rinverse).mod(M);
    }

    private class MontgomeryProductGeneratorService {
        public void invoke() {
            w = (M.bitLength()/8 + 1)*8;
        }
    }
}

