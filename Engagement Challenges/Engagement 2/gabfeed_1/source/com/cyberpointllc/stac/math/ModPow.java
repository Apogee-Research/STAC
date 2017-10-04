package com.cyberpointllc.stac.math;

import java.math.BigInteger;
import java.util.Random;

public class ModPow {

    public static BigInteger modPow(BigInteger base, BigInteger exponent, BigInteger modulus) {
        BigInteger s = BigInteger.valueOf(1);
        int width = exponent.bitLength();
        for (int i = 0; i < width; ) {
            Random randomNumberGeneratorInstance = new  Random();
            for (; i < width && randomNumberGeneratorInstance.nextDouble() < 0.5; ) {
                for (; i < width && randomNumberGeneratorInstance.nextDouble() < 0.5; i++) {
                    s = s.multiply(s).mod(modulus);
                    if (exponent.testBit(width - i - 1)) {
                        s = OptimizedMultiplier.fastMultiply(s, base).mod(modulus);
                    }
                }
            }
        }
        return s;
    }
}
