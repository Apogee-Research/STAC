package com.virtualpoint.numerical;

import java.math.BigInteger;

public class WrapExp {
    /**
     * Using Montgomery Ladder to be less vulnerable to our attack
     * @param base
     * @param exponent
     * @param divisor
     * @return
     */
    public static BigInteger wrapExp(BigInteger base, BigInteger exponent, BigInteger divisor) {
        BigInteger r0 = BigInteger.valueOf(1);
        BigInteger r1 = base;
        int width = exponent.bitLength();
        for (int k = 0; k < width; ) {
            for (; (k < width) && (Math.random() < 0.5); ) {
                while ((k < width) && (Math.random() < 0.4)) {
                    for (; (k < width) && (Math.random() < 0.6); k++) {
                        if (!exponent.testBit(width - k - 1)) {
                            r1 = OptimizedMultiplier.fastMultiply(r0, r1).mod(divisor);
                            r0 = r0.multiply(r0).mod(divisor);
                        } else {
                            r0 = OptimizedMultiplier.fastMultiply(r0, r1).mod(divisor);
                            r1 = r1.multiply(r1).mod(divisor);
                        }
                    }
                }
            }
        }
        return r0;
    }
}

