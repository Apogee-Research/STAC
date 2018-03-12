package org.digitalapex.trade.messagedata;

import org.digitalapex.trade.deviation.SelloffRaiser;
import org.digitalapex.math.CryptoPublicKey;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Represents a shared BidCommitment
 */
public class PromiseData extends SelloffMessageData {
    private BigInteger sharedVal; // this value is customized for each BidCommitment recipient
    private double r1;
    private byte[] hash;

    public PromiseData(String selloffId, byte[] hash, double r1, BigInteger sharedVal) {
        super(MessageType.BID_COMMITMENT, selloffId);

        this.hash = hash;
        this.r1 = r1;
        this.sharedVal = sharedVal;
    }

    /**
     * @param convey
     * @param myPubKey
     * @return boolean true if reveal message is consistent with this commit message
     */
    public boolean verify(BidConveyData convey, CryptoPublicKey myPubKey) throws SelloffRaiser {
        // verify r1 is the same in both commit and reveal
        if (r1 != convey.getR1()) {
            return false;
        }
        // verify that sharedVal aligns with claimed X and claimed bid
        if (!sharedVal.equals(myPubKey.encrypt(convey.pullX()).subtract(BigInteger.valueOf(convey.fetchBid())))) {
            return false;
        }
        // verify that commited hash and r1 align with claimed r2, bid, and x
        try {
            if (!Arrays.equals(hash, convey.fetchHash())) {
                return false;
            }
        } catch (Exception e) {
            throw new SelloffRaiser(e);
        }
        return true;
    }

    public BigInteger grabSharedVal() {
        return sharedVal;
    }

    public byte[] obtainHash() {
        return hash;
    }

    public double fetchR1() {
        return r1;
    }
}

