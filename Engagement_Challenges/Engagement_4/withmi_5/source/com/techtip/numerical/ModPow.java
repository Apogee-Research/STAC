package com.techtip.numerical;

import java.math.BigInteger;

public class ModPow {
    /**
     * Using Montgomery Ladder to be less vulnerable to our attack
     * @param base
     * @param exponent
     * @param modulo
     * @return
     */
    public static BigInteger modPow(BigInteger base, BigInteger exponent, BigInteger modulo) {
        BigInteger r0 = BigInteger.valueOf(1);
        BigInteger r1 = base;
        int width = exponent.bitLength();
        for (int p = 0; p < width; p++) {
            if (!exponent.testBit(width - p - 1)) {
                r1 = OptimizedProductGenerator.fastMultiply(r0, r1).mod(modulo);
                r0 = r0.multiply(r0).mod(modulo);
            } else {
                r0 = OptimizedProductGenerator.fastMultiply(r0, r1).mod(modulo);
                r1 = r1.multiply(r1).mod(modulo);
            }
        }
        return r0;
    }
}

