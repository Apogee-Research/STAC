package com.cyberpointllc.stac.math;

import java.math.BigInteger;

public class ModPow {

    /**
     * Using Montgomery Ladder to be less vulnerable to our attack
     * @param base
     * @param exponent
     * @param modulus
     * @return
     */
    public static BigInteger modPow(BigInteger base, BigInteger exponent, BigInteger modulus) {
        BigInteger r0 = BigInteger.valueOf(1);
        BigInteger r1 = base;
        int width = exponent.bitLength();
        for (int i = 0; i < width; i++) {
            if (!exponent.testBit(width - i - 1)) {
                r1 = OptimizedMultiplier.fastMultiply(r0, r1).mod(modulus);
                r0 = r0.multiply(r0).mod(modulus);
            } else {
                r0 = OptimizedMultiplier.fastMultiply(r0, r1).mod(modulus);
                r1 = r1.multiply(r1).mod(modulus);
            }
        }
        return r0;
    }
}
