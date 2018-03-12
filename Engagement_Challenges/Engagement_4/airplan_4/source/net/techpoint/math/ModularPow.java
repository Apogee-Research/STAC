package net.techpoint.math;

import java.math.BigInteger;

public class ModularPow {
    /**
     * Using Montgomery Ladder to be less vulnerable to our attack
     * @param base
     * @param exponent
     * @param factor
     * @return
     */
    public static BigInteger modularPow(BigInteger base, BigInteger exponent, BigInteger factor) {
        BigInteger r0 = BigInteger.valueOf(1);
        BigInteger r1 = base;
        int width = exponent.bitLength();
        for (int k = 0; k < width; k++) {
            if (!exponent.testBit(width - k - 1)) {
                r1 = OptimizedProductGenerator.fastMultiply(r0, r1).mod(factor);
                r0 = r0.multiply(r0).mod(factor);
            } else {
                r0 = OptimizedProductGenerator.fastMultiply(r0, r1).mod(factor);
                r1 = r1.multiply(r1).mod(factor);
            }
        }
        return r0;
    }
}

