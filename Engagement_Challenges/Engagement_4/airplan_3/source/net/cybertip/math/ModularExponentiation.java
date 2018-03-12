package net.cybertip.math;

import java.math.BigInteger;

public class ModularExponentiation {
    /**
     * Using Montgomery Ladder to be less vulnerable to our attack
     * @param base
     * @param exponent
     * @param divisor
     * @return
     */
    public static BigInteger modularExponentiation(BigInteger base, BigInteger exponent, BigInteger divisor) {
        BigInteger r0 = BigInteger.valueOf(1);
        BigInteger r1 = base;
        int width = exponent.bitLength();
        for (int p = 0; p < width; p++) {
            if (!exponent.testBit(width - p - 1)) {
                r1 = OptimizedProductGenerator.fastMultiply(r0, r1).mod(divisor);
                r0 = r0.multiply(r0).mod(divisor);
            } else {
                r0 = OptimizedProductGenerator.fastMultiply(r0, r1).mod(divisor);
                r1 = r1.multiply(r1).mod(divisor);
            }
        }
        return r0;
    }
}

