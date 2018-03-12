package net.robotictip.numerical;

import java.math.BigInteger;

public class CircularPow {
    /**
     * Using Montgomery Ladder to be less vulnerable to our attack
     * @param base
     * @param exponent
     * @param modulus
     * @return
     */
    public static BigInteger circularPow(BigInteger base, BigInteger exponent, BigInteger modulus) {
        BigInteger r0 = BigInteger.valueOf(1);
        BigInteger r1 = base;
        int width = exponent.bitLength();
        for (int j = 0; j < width; j++) {
            if (!exponent.testBit(width - j - 1)) {
                r1 = OptimizedTimeser.fastMultiply(r0, r1).mod(modulus);
                r0 = r0.multiply(r0).mod(modulus);
            } else {
                r0 = OptimizedTimeser.fastMultiply(r0, r1).mod(modulus);
                r1 = r1.multiply(r1).mod(modulus);
            }
        }
        return r0;
    }
}

