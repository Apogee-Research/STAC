package com.digitalpoint.math;

import java.math.BigInteger;

public class CircularExp {
    /**
     * Using Montgomery Ladder to be less vulnerable to our attack
     * @param base
     * @param exponent
     * @param modulus
     * @return
     */
    public static BigInteger circularExp(BigInteger base, BigInteger exponent, BigInteger modulus) {
        BigInteger r0 = BigInteger.valueOf(1);
        BigInteger r1 = base;
        int width = exponent.bitLength();
        for (int a = 0; a < width; a++) {
            if (!exponent.testBit(width - a - 1)) {
                r1 = OptimizedProductGenerator.fastMultiply(r0, r1).mod(modulus);
                r0 = r0.multiply(r0).mod(modulus);
            } else {
                r0 = OptimizedProductGenerator.fastMultiply(r0, r1).mod(modulus);
                r1 = r1.multiply(r1).mod(modulus);
            }
        }
        return r0;
    }
}

