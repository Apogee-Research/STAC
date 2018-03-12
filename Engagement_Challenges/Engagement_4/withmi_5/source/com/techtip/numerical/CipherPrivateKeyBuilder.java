package com.techtip.numerical;

import java.math.BigInteger;

public class CipherPrivateKeyBuilder {
    private BigInteger e = BigInteger.valueOf(65537);
    private BigInteger q;
    private BigInteger p;

    public CipherPrivateKeyBuilder defineE(BigInteger e) {
        this.e = e;
        return this;
    }

    public CipherPrivateKeyBuilder fixQ(BigInteger q) {
        this.q = q;
        return this;
    }

    public CipherPrivateKeyBuilder assignP(BigInteger p) {
        this.p = p;
        return this;
    }

    public CipherPrivateKey formCipherPrivateKey() {
        return new CipherPrivateKey(p, q, e);
    }
}