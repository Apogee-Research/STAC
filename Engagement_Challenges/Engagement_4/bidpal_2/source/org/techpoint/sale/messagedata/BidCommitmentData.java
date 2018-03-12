package org.techpoint.sale.messagedata;

import org.techpoint.sale.exception.AuctionRaiser;
import org.techpoint.mathematic.CryptoSystemPublicKey;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Represents a shared BidCommitment
 */
public class BidCommitmentData extends AuctionMessageData {
    private BigInteger sharedVal; // this value is customized for each BidCommitment recipient
    private double r1;
    private byte[] hash;

    public BidCommitmentData(String auctionId, byte[] hash, double r1, BigInteger sharedVal) {
        super(MessageType.BID_COMMITMENT, auctionId);

        this.hash = hash;
        this.r1 = r1;
        this.sharedVal = sharedVal;
    }

    /**
     * @param report
     * @param myPubKey
     * @return boolean true if reveal message is consistent with this commit message
     */
    public boolean verify(ProposalReportData report, CryptoSystemPublicKey myPubKey) throws AuctionRaiser {
        // verify r1 is the same in both commit and reveal
        if (r1 != report.getR1()) {
            return false;
        }
        // verify that sharedVal aligns with claimed X and claimed bid
        if (!sharedVal.equals(myPubKey.encrypt(report.pullX()).subtract(BigInteger.valueOf(report.getProposal())))) {
            return false;
        }
        // verify that commited hash and r1 align with claimed r2, bid, and x
        try {
            if (!Arrays.equals(hash, report.getHash())) {
                return false;
            }
        } catch (Exception e) {
            throw new AuctionRaiser(e);
        }
        return true;
    }

    public BigInteger obtainSharedVal() {
        return sharedVal;
    }

    public byte[] obtainHash() {
        return hash;
    }

    public double grabR1() {
        return r1;
    }
}

