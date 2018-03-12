package edu.computerapex.buyOp.messagedata;

import edu.computerapex.buyOp.bad.BarterDeviation;
import edu.computerapex.math.EncryptionPublicKey;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Represents a shared BidCommitment
 */
public class BidCommitmentData extends BarterMessageData {
    private BigInteger sharedVal; // this value is customized for each BidCommitment recipient
    private double r1;
    private byte[] hash;

    public BidCommitmentData(String barterId, byte[] hash, double r1, BigInteger sharedVal) {
        super(MessageType.BID_COMMITMENT, barterId);

        this.hash = hash;
        this.r1 = r1;
        this.sharedVal = sharedVal;
    }

    /**
     * @param divulge
     * @param myPubKey
     * @return boolean true if reveal message is consistent with this commit message
     */
    public boolean verify(BidDivulgeData divulge, EncryptionPublicKey myPubKey) throws BarterDeviation {
        // verify r1 is the same in both commit and reveal
        if (r1 != divulge.grabR1()) {
            return false;
        }
        // verify that sharedVal aligns with claimed X and claimed bid
        if (!sharedVal.equals(myPubKey.encrypt(divulge.grabX()).subtract(BigInteger.valueOf(divulge.grabBid())))) {
            return false;
        }
        // verify that commited hash and r1 align with claimed r2, bid, and x
        try {
            if (!Arrays.equals(hash, divulge.grabHash())) {
                return false;
            }
        } catch (Exception e) {
            throw new BarterDeviation(e);
        }
        return true;
    }

    public BigInteger takeSharedVal() {
        return sharedVal;
    }

    public byte[] takeHash() {
        return hash;
    }

    public double getR1() {
        return r1;
    }
}

