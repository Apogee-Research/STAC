package edu.computerapex.buyOp;

import edu.computerapex.buyOp.bad.IllegalOperationDeviation;
import edu.computerapex.buyOp.bad.RebidDeviation;
import edu.computerapex.buyOp.messagedata.BidCommitmentData;
import edu.computerapex.buyOp.messagedata.BidDivulgeData;
import edu.computerapex.dialogs.CommunicationsPublicIdentity;
import edu.computerapex.logging.Logger;
import edu.computerapex.logging.LoggerFactory;

import java.util.HashMap;

public class Barter {
    private static final Logger logger = LoggerFactory.takeLogger(Barter.class);

    private String barterId;
    private String barterDescription;
    private HashMap<CommunicationsPublicIdentity, BidData> offers = new HashMap<CommunicationsPublicIdentity, BidData>();
    private boolean winning = true; // is my bid as big as all I've seen so far
    private BidDivulgeData myCommitment;
    private CommunicationsPublicIdentity seller;
    private boolean sellerIsMe;
    private boolean ended = false; // has bidding period ended?
    private String winner;
    private int winningBid;
    private BiddersStatus biddersStatus = new BiddersStatus();// data useful for seller in choosing winner (and deciding whether he should wait to do so)

    public Barter(String barterId, CommunicationsPublicIdentity seller, String barterDescription, boolean iAmSeller) {
        this.barterId = barterId;
        this.barterDescription = barterDescription;
        this.seller = seller;
        this.sellerIsMe = iAmSeller;
    }

    public String fetchStatusString() {
        String status = "description: " + barterDescription + "\n";
        if (amISeller()) {
            status += "I am the seller --" + seller.takeId() + ". \n";
        } else {
            status += "Seller is " + seller.takeId() + ".\n";
        }
        if (!ended) {
            status += "Status: open. \n";
        } else if (winner != null) {
            status += "Status: won by " + winner + " for $" + winningBid + ". \n";
        } else {
            status += "Status: bidding ended, but winner not yet announced. \n";
        }
        if (myCommitment == null) {
            status += "I did not bid.\n";
        } else {
            status += "I bid $" + myCommitment.grabBid() + ".\n";
        }
        return status;
    }

    public boolean amISeller() {
        return sellerIsMe;
    }


    public void addCommitment(CommunicationsPublicIdentity participant, BidCommitmentData commitment) throws RebidDeviation {
        BidData data;
        if (!offers.containsKey(participant)) {
            data = new BidData(commitment);
            offers.put(participant, data);
            biddersStatus.addBidder(participant);
        } else {
            throw new RebidDeviation("User " + participant + " has sent more than one bid for auction " + barterId);
        }
    }

    public void recordMyCommit(BidDivulgeData commitment, CommunicationsPublicIdentity myIdentity) throws IllegalOperationDeviation, RebidDeviation {

        myCommitment = commitment;
        biddersStatus.addBidder(myIdentity);
        addCommitment(myIdentity, commitment.fetchCommitmentData(myIdentity));
        addMeasurement(myIdentity, true); // my bid is as big as my bid

    }

    public BidDivulgeData takeMyCommit() {
        return myCommitment;
    }

    // get user's bid commitment on this auction
    public BidCommitmentData fetchBidCommitment(CommunicationsPublicIdentity participant) {
        return offers.get(participant).pullCommitment();
    }

    public void removeBid(CommunicationsPublicIdentity participant) {
        offers.remove(participant);
        biddersStatus.removeBidder(participant);
    }


    public void addMeasurement(CommunicationsPublicIdentity participant, boolean mineAsBig) throws IllegalOperationDeviation {
        if (!mineAsBig) {
            winning = false;
        }
        BidData data = offers.get(participant);
        if (data == null) {
            StringBuilder builder = new StringBuilder();
            for (CommunicationsPublicIdentity bidder : offers.keySet()) {
                addMeasurementAid(builder, bidder);
            }
            throw new IllegalOperationDeviation("Received bid comparison from " + participant.takeId() +
                    " but never received a bid commitment. Have these bidders: \n" + builder.toString());
        }
        data.setMeasurement(mineAsBig);
    }

    private void addMeasurementAid(StringBuilder builder, CommunicationsPublicIdentity bidder) {
        builder.append("have bidder " + bidder.toString());
        builder.append('\n');
    }

    public void addConcession(CommunicationsPublicIdentity participant) {
        BidData data = offers.get(participant);
        data.concede();
        biddersStatus.addConcession(participant);
    }

    public void addWinClaim(CommunicationsPublicIdentity participant, int bid) {
        BidData data = offers.get(participant);
        data.claim(bid);
        biddersStatus.addWinClaim(participant, bid);
    }

    /**
     * @param participantId
     * @return the value of the claimed winning bid from userId, if there is one; null if not
     */
    public Integer fetchExpectedWinningBid(String participantId) {
        for (CommunicationsPublicIdentity participant : offers.keySet()) {
            Integer barterAid = fetchExpectedWinningBidUtility(participantId, participant);
            if (barterAid != null) return barterAid;
        }
        return null;
    }

    private Integer fetchExpectedWinningBidUtility(String participantId, CommunicationsPublicIdentity participant) {
        BarterAid barterAid = new BarterAid(participantId, participant).invoke();
        if (barterAid.is()) return barterAid.fetchData().getClaimingBid();
        return null;
    }


    public BiddersStatus obtainBiddersStatus() {
        return biddersStatus;
    }


    /**
     * This should only be used in the case where we discovered someone lied in their comparison message
     * and we have to figure out if we would have won had they not.  This shouldn't happen with normal use.
     *
     * @return the number of bids that are greater than mine
     */
    public int countBidsAboveMine() {
        int count = 0;
        for (BidData data : offers.values()) {
            if (!data.mineAsBig) {
                count++;
            }
        }
        return count;
    }

    /**
     * @return is the revealedBid consistent with the comparison message conn sent for this auction?
     */
    public boolean isConsistentWithMeasurement(CommunicationsPublicIdentity participant, int revealedBid) {
        if (myCommitment == null) {
            return true; // I didn't bid, so I have no info to check for consistency
        } else {
            boolean claimedResult = (takeMyCommit().grabBid() >= revealedBid);
            boolean recordedResult = wasMineAsBig(participant);
            logger.info("claimedResult: mineAsBig? " + claimedResult);
            logger.info("recordedResult: mineAsBig? " + recordedResult);
            return claimedResult == recordedResult;
        }
    }

    public boolean wasMineAsBig(CommunicationsPublicIdentity participant) {
        return offers.get(participant).mineAsBig;
    }

    public boolean amIWinning() {
        // Note: we won't worry about the case where someone has bid but we haven't gotten their comparison yet
        // if they were the real winner, they will also claim such, and the seller will know the truth
        return myCommitment != null && winning;
    }

    public boolean didIBid() {
        return myCommitment != null;
    }

    public void fixOver() {
        this.ended = true;
    }

    public boolean isOver() {
        return this.ended;
    }

    public boolean verifySeller(CommunicationsPublicIdentity participant) {
        return participant.equals(seller);
    }

    public boolean verifyHighest(int claimedWinningBid) {
        BiddersStatus status = obtainBiddersStatus();
        return status.verifyHighest(claimedWinningBid);
    }

    public void assignWinner(String winner, int winningBid) {
        this.winner = winner;
        this.winningBid = winningBid;
    }

//////////////////////////  Class BidData ///////////////////////////////////////

    // class to hold any data regarding a bid from another user
    private class BidData {
        // class representing a user's claim (or concession) as winner of an auction
        private class ClaimOrConcession {
            private int bid = -1; // will only be set if conceded = false

            // constructor for claiming win
            private ClaimOrConcession(int bid) {
                this.bid = bid;
            }

            //constructor for concession
            private ClaimOrConcession() {
            }

            private boolean isConceded() {
                return (bid < 0);
            }
        }

        private BidCommitmentData commitment;
        private Boolean mineAsBig = null; // Is my bid as big as this one?  will be null until comparison has occurred
        private ClaimOrConcession claimStatus = null;

        private BidData(BidCommitmentData com) {
            commitment = com;
        }

        private BidCommitmentData pullCommitment() {
            return commitment;
        }

        private void setMeasurement(boolean mineAsBig) {
            this.mineAsBig = mineAsBig;
        }

        // mark this user as having conceded this auction
        private void claim(int bid) {
            this.claimStatus = new ClaimOrConcession(bid);
        }

        // mark this user as having claimed winnership in this auction
        private void concede() {
            this.claimStatus = new ClaimOrConcession(); // conceded claim
        }

        /**
         * @return claimed winner bid if user claimed to win, null otherwise
         */
        private Integer getClaimingBid() {
            if (claimStatus == null || claimStatus.isConceded()) {
                return null;
            } else {
                return claimStatus.bid;
            }
        }
    }

    private class BarterAid {
        private boolean myResult;
        private String participantId;
        private CommunicationsPublicIdentity participant;
        private BidData data;

        public BarterAid(String participantId, CommunicationsPublicIdentity participant) {
            this.participantId = participantId;
            this.participant = participant;
        }

        boolean is() {
            return myResult;
        }

        public BidData fetchData() {
            return data;
        }

        public BarterAid invoke() {
            if (participant.takeId().equals(participantId)) {
                return invokeFunction();
            }
            myResult = false;
            return this;
        }

        private BarterAid invokeFunction() {
            data = offers.get(participant);
            myResult = true;
            return this;
        }
    }
}
