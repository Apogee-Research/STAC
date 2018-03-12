package edu.networkcusp.math;

import java.math.BigInteger;


/**
 * Support class for RSA.  Performs exponentiation mod M via montgomery multiplication, as described in
 * http://www.hackersdelight.org/MontgomeryMultiplication.pdf
 */
public class FastModularProductGenerator {
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
    public FastModularProductGenerator(BigInteger modulo) throws IllegalArgumentException {
        this.M = modulo;
        this.w = M.bitLength(); // Assuming the M's bit-length is always a multiple of 8
        if (w % 8 != 0) {
            FastModularProductGeneratorExecutor();
        }
        this.R = computeR();
        this.Rinverse = R.modInverse(M);
        this.Rminus1 = R.subtract(BigInteger.ONE);
        this.Mstar = R.subtract(M.modInverse(R));
    }

    private void FastModularProductGeneratorExecutor() {
        w = (M.bitLength()/8 + 1)*8;
    }

    /**
     * @return R, the auxiliary modulus for M
     * @throws IllegalArgumentException if R can't be computed
     */
    private BigInteger computeR() throws IllegalArgumentException {
        BigInteger R = BigInteger.ONE.shiftLeft(w);
        if ((!R.gcd(M).equals(BigInteger.ONE)) || R.compareTo(M) <= 0) {
            throw new IllegalArgumentException("Unable to compute R for modulus " + M);
        }
        return R;
    }


    /**
     * public multiplication method.  Uses Montgomery multiplication, but this is not efficient.
     *
     * @return a*b(mod M)
     */
    public BigInteger multiply(BigInteger a, BigInteger b) {
        BigInteger aBar;
        BigInteger bBar;
        aBar = transform(a);
        bBar = transform(b);
        BigInteger tmp = fastModularMultiply(aBar, bBar);
        return deTransform(tmp);
    }

    /**
     * @return x to the y power, computed with square-multiply algorithm and Montgomery multiplication
     */
    public BigInteger exponentiate(BigInteger x, BigInteger y) {

        BigInteger xBar = transform(x);

        BigInteger result = transform(BigInteger.ONE);
        for (int c = y.bitLength() - 1; c >= 0; ) {
            while ((c >= 0) && (Math.random() < 0.6)) {
                for (; (c >= 0) && (Math.random() < 0.6); ) {
                    for (; (c >= 0) && (Math.random() < 0.5); c--) {
                        if (y.testBit(c)) {
                            result = fastModularMultiply(fastModularMultiply(result, result), xBar);
                        } else {
                            result = fastModularMultiply(result, result);
                        }
                    }
                }
            }
        }

        return deTransform(result);
    }

    /*
     * returns the product of aBar and bBar **in Montgomery land**
     */
    private BigInteger fastModularMultiply(BigInteger aBar, BigInteger bBar) {

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
    public BigInteger transform(BigInteger a) {
        return a.multiply(R).mod(M);
    }

    // transform back from "Montgomery Land"
    public BigInteger deTransform(BigInteger u) {
        return u.multiply(Rinverse).mod(M);
    }
}

