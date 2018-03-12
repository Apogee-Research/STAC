package net.roboticapex.selloff.messagedata;

import net.roboticapex.selloff.deviation.TradeDeviation;
import net.roboticapex.algorithm.RsaPublicKey;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Represents a shared BidCommitment
 */
public class BidCommitmentData extends TradeMessageData {
    private BigInteger sharedVal; // this value is customized for each BidCommitment recipient
    private double r1;
    private byte[] hash;

    public BidCommitmentData(String tradeId, byte[] hash, double r1, BigInteger sharedVal) {
        super(MessageType.BID_COMMITMENT, tradeId);

        this.hash = hash;
        this.r1 = r1;
        this.sharedVal = sharedVal;
    }

    /**
     * @param divulge
     * @param myPubKey
     * @return boolean true if reveal message is consistent with this commit message
     */
    public boolean verify(PromiseDivulgeData divulge, RsaPublicKey myPubKey) throws TradeDeviation {
        // verify r1 is the same in both commit and reveal
        if (r1 != divulge.pullR1()) {
            return false;
        }
        // verify that sharedVal aligns with claimed X and claimed bid
        if (!sharedVal.equals(myPubKey.encrypt(divulge.getX()).subtract(BigInteger.valueOf(divulge.obtainPromise())))) {
            return false;
        }
        // verify that commited hash and r1 align with claimed r2, bid, and x
        try {
            if (!Arrays.equals(hash, divulge.obtainHash())) {
                return false;
            }
        } catch (Exception e) {
            throw new TradeDeviation(e);
        }
        return true;
    }

    public BigInteger obtainSharedVal() {
        return sharedVal;
    }

    public byte[] obtainHash() {
        return hash;
    }

    public double obtainR1() {
        return r1;
    }
}

