package edu.networkcusp.math;

import java.math.BigInteger;

public class PrivateProtocolsPrivateKeyBuilder {
    private BigInteger e = BigInteger.valueOf(65537);
    private BigInteger q;
    private BigInteger p;

    public PrivateProtocolsPrivateKeyBuilder assignE(BigInteger e) {
        this.e = e;
        return this;
    }

    public PrivateProtocolsPrivateKeyBuilder setQ(BigInteger q) {
        this.q = q;
        return this;
    }

    public PrivateProtocolsPrivateKeyBuilder setP(BigInteger p) {
        this.p = p;
        return this;
    }

    public PrivateCommsPrivateKey formPrivateProtocolsPrivateKey() {
        return new PrivateCommsPrivateKey(p, q, e);
    }
}