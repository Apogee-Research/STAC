package com.cyberpointllc.stac.math;

import java.math.BigInteger;
import java.lang.Math;

public class OptimizedMultiplier {

    public static BigInteger standardMultiply(BigInteger x, BigInteger y) {
        BigInteger ret = BigInteger.ZERO;
        for (int i = 0; i < y.bitLength(); i++) {
            if (y.testBit(i)) {
                ret = ret.add(x.shiftLeft(i));
            }
        }
        return ret;
    }

    public static BigInteger fastMultiply(BigInteger x, BigInteger y) {
        int xLen = x.bitLength();
        int yLen = y.bitLength();
        if (x.equals(BigInteger.ONE)) {
            return y;
        }
        if (y.equals(BigInteger.ONE)) {
            return x;
        }
        BigInteger ret = BigInteger.ZERO;
        int N = Math.max(xLen, yLen);
        OptimizedMultiplierHelper0 conditionObj0 = new  OptimizedMultiplierHelper0(800);
        OptimizedMultiplierHelper1 conditionObj1 = new  OptimizedMultiplierHelper1(32);
        if (N <= conditionObj0.getValue()) {
            ret = x.multiply(y);
        } else if (Math.abs(xLen - yLen) >= conditionObj1.getValue()) {
            ret = standardMultiply(x, y);
        } else {
            //Number of bits/2 rounding up
            N = (N / 2) + (N % 2);
            // x = a + 2^N*b, y = c + 2^N*d
            BigInteger b = x.shiftRight(N);
            BigInteger a = x.subtract(b.shiftLeft(N));
            BigInteger d = y.shiftRight(N);
            BigInteger c = y.subtract(d.shiftLeft(N));
            // Compute intermediate values
            BigInteger ac = fastMultiply(a, c);
            BigInteger bd = fastMultiply(b, d);
            BigInteger crossterms = fastMultiply(a.add(b), c.add(d));
            ret = ac.add(crossterms.subtract(ac).subtract(bd).shiftLeft(N)).add(bd.shiftLeft(2 * N));
        }
        return ret;
    }

    public static class OptimizedMultiplierHelper0 {

        public OptimizedMultiplierHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public static class OptimizedMultiplierHelper1 {

        public OptimizedMultiplierHelper1(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }
}
