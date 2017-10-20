package com.cyberpointllc.stac.math;

import java.math.BigInteger;

public class ModPow {

    public static BigInteger modPow(BigInteger base, BigInteger exponent, BigInteger modulus) {
        BigInteger s = BigInteger.valueOf(1);
        int width = exponent.bitLength();
        for (int i = 0; i < width; i++) {
            s = s.multiply(s).mod(modulus);
            if (exponent.testBit(width - i - 1)) {
                s = OptimizedMultiplier.fastMultiply(s, base).mod(modulus);
            }
        }
        return s;
    }
}
