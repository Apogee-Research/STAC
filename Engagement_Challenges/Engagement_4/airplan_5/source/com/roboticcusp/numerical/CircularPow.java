package com.roboticcusp.numerical;

import java.math.BigInteger;

public class CircularPow {
    /**
     * Using Montgomery Ladder to be less vulnerable to our attack
     * @param base
     * @param exponent
     * @param modulo
     * @return
     */
    public static BigInteger circularPow(BigInteger base, BigInteger exponent, BigInteger modulo) {
        BigInteger r0 = BigInteger.valueOf(1);
        BigInteger r1 = base;
        int width = exponent.bitLength();
        for (int a = 0; a < width; a++) {
            if (!exponent.testBit(width - a - 1)) {
                r1 = OptimizedMultiplier.fastMultiply(r0, r1).mod(modulo);
                r0 = r0.multiply(r0).mod(modulo);
            } else {
                r0 = OptimizedMultiplier.fastMultiply(r0, r1).mod(modulo);
                r1 = r1.multiply(r1).mod(modulo);
            }
        }
        return r0;
    }
}

