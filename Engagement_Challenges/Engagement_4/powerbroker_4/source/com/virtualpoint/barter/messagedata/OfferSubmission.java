package com.virtualpoint.barter.messagedata;

import com.virtualpoint.barter.failure.BarterTrouble;
import com.virtualpoint.numerical.CipherPublicKey;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Represents a shared BidCommitment
 */
public class OfferSubmission extends BarterMessageData {
    private BigInteger sharedVal; // this value is customized for each BidCommitment recipient
    private double r1;
    private byte[] hash;

    public OfferSubmission(String barterId, byte[] hash, double r1, BigInteger sharedVal) {
        super(MessageType.BID_COMMITMENT, barterId);

        this.hash = hash;
        this.r1 = r1;
        this.sharedVal = sharedVal;
    }

    /**
     * @param convey
     * @param myPubKey
     * @return boolean true if reveal message is consistent with this commit message
     */
    public boolean verify(BidConveyData convey, CipherPublicKey myPubKey) throws BarterTrouble {
        // verify r1 is the same in both commit and reveal
        if (r1 != convey.fetchR1()) {
            return false;
        }
        // verify that sharedVal aligns with claimed X and claimed bid
        if (!sharedVal.equals(myPubKey.encrypt(convey.obtainX()).subtract(BigInteger.valueOf(convey.takeBid())))) {
            return false;
        }
        // verify that commited hash and r1 align with claimed r2, bid, and x
        try {
            if (!Arrays.equals(hash, convey.getHash())) {
                return false;
            }
        } catch (Exception e) {
            throw new BarterTrouble(e);
        }
        return true;
    }

    public BigInteger obtainSharedVal() {
        return sharedVal;
    }

    public byte[] grabHash() {
        return hash;
    }

    public double fetchR1() {
        return r1;
    }
}

