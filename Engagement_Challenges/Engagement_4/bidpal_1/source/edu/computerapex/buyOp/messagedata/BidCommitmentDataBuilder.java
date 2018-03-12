package edu.computerapex.buyOp.messagedata;

import java.math.BigInteger;

public class BidCommitmentDataBuilder {
    private BigInteger sharedVal;
    private String barterId;
    private byte[] hash;
    private double r1;

    public BidCommitmentDataBuilder setSharedVal(BigInteger sharedVal) {
        this.sharedVal = sharedVal;
        return this;
    }

    public BidCommitmentDataBuilder setBarterId(String barterId) {
        this.barterId = barterId;
        return this;
    }

    public BidCommitmentDataBuilder setHash(byte[] hash) {
        this.hash = hash;
        return this;
    }

    public BidCommitmentDataBuilder defineR1(double r1) {
        this.r1 = r1;
        return this;
    }

    public BidCommitmentData generateBidCommitmentData() {
        return new BidCommitmentData(barterId, hash, r1, sharedVal);
    }
}