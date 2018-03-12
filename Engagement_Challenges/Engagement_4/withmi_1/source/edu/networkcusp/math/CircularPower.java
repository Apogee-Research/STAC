package edu.networkcusp.math;

import java.math.BigInteger;

public class CircularPower {
    /**
     * Using Montgomery Ladder to be less vulnerable to our attack
     * @param base
     * @param exponent
     * @param floormod
     * @return
     */
    public static BigInteger circularPower(BigInteger base, BigInteger exponent, BigInteger floormod) {
        BigInteger r0 = BigInteger.valueOf(1);
        BigInteger r1 = base;
        int width = exponent.bitLength();
        for (int j = 0; j < width; j++) {
            if (!exponent.testBit(width - j - 1)) {
                r1 = OptimizedTimeser.fastMultiply(r0, r1).mod(floormod);
                r0 = r0.multiply(r0).mod(floormod);
            } else {
                r0 = OptimizedTimeser.fastMultiply(r0, r1).mod(floormod);
                r1 = r1.multiply(r1).mod(floormod);
            }
        }
        return r0;
    }
}

