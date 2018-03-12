package com.digitalpoint.math;

import java.math.BigInteger;

public class CryptoPrivateKeyBuilder {
    private BigInteger e = BigInteger.valueOf(65537);
    private BigInteger q;
    private BigInteger p;

    public CryptoPrivateKeyBuilder setE(BigInteger e) {
        this.e = e;
        return this;
    }

    public CryptoPrivateKeyBuilder fixQ(BigInteger q) {
        this.q = q;
        return this;
    }

    public CryptoPrivateKeyBuilder defineP(BigInteger p) {
        this.p = p;
        return this;
    }

    public CryptoPrivateKey makeCryptoPrivateKey() {
        return new CryptoPrivateKey(p, q, e);
    }
}