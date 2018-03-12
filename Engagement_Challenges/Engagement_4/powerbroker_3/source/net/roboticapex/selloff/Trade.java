package net.roboticapex.selloff;

import net.roboticapex.selloff.deviation.IllegalOperationDeviation;
import net.roboticapex.selloff.deviation.RebidDeviation;
import net.roboticapex.selloff.messagedata.BidCommitmentData;
import net.roboticapex.selloff.messagedata.PromiseDivulgeData;
import net.roboticapex.senderReceivers.SenderReceiversPublicIdentity;
import net.roboticapex.logging.Logger;
import net.roboticapex.logging.LoggerFactory;

import java.util.HashMap;

public class Trade {
    private static final Logger logger = LoggerFactory.fetchLogger(Trade.class);

    private String tradeId;
    private String tradeDescription;
    private HashMap<SenderReceiversPublicIdentity, PromiseData> promises = new HashMap<SenderReceiversPublicIdentity, PromiseData>();
    private boolean winning = true; // is my bid as big as all I've seen so far
    private PromiseDivulgeData myCommitment;
    private SenderReceiversPublicIdentity seller;
    private boolean sellerIsMe;
    private boolean ended = false; // has bidding period ended?
    private String winner;
    private int winningPromise;
    private BiddersStatus biddersStatus = new BiddersStatusBuilder().makeBiddersStatus();// data useful for seller in choosing winner (and deciding whether he should wait to do so)

    public Trade(String tradeId, SenderReceiversPublicIdentity seller, String tradeDescription, boolean iAmSeller) {
        this.tradeId = tradeId;
        this.tradeDescription = tradeDescription;
        this.seller = seller;
        this.sellerIsMe = iAmSeller;
    }

    public String grabStatusString() {
        String status = "description: " + tradeDescription + "\n";
        if (amISeller()) {
            status += "I am the seller --" + seller.obtainId() + ". \n";
        } else {
            status += "Seller is " + seller.obtainId() + ".\n";
        }
        if (!ended) {
            status += "Status: open. \n";
        } else if (winner != null) {
            status += "Status: won by " + winner + " for $" + winningPromise + ". \n";
        } else {
            status += "Status: bidding ended, but winner not yet announced. \n";
        }
        if (myCommitment == null) {
            status += "I did not bid.\n";
        } else {
            status += "I bid $" + myCommitment.obtainPromise() + ".\n";
        }
        return status;
    }

    public boolean amISeller() {
        return sellerIsMe;
    }


    public void addCommitment(SenderReceiversPublicIdentity user, BidCommitmentData commitment) throws RebidDeviation {
        PromiseData data;
        if (!promises.containsKey(user)) {
            data = new PromiseData(commitment);
            promises.put(user, data);
            biddersStatus.addBidder(user);
        } else {
            throw new RebidDeviation("User " + user + " has sent more than one bid for auction " + tradeId);
        }
    }

    public void recordMyCommit(PromiseDivulgeData commitment, SenderReceiversPublicIdentity myIdentity) throws IllegalOperationDeviation, RebidDeviation {

        myCommitment = commitment;
        biddersStatus.addBidder(myIdentity);
        addCommitment(myIdentity, commitment.grabCommitmentData(myIdentity));
        addTesting(myIdentity, true); // my bid is as big as my bid

    }

    public PromiseDivulgeData takeMyCommit() {
        return myCommitment;
    }

    // get user's bid commitment on this auction
    public BidCommitmentData getPromiseCommitment(SenderReceiversPublicIdentity user) {
        return promises.get(user).obtainCommitment();
    }

    public void removePromise(SenderReceiversPublicIdentity user) {
        promises.remove(user);
        biddersStatus.removeBidder(user);
    }


    public void addTesting(SenderReceiversPublicIdentity user, boolean mineAsBig) throws IllegalOperationDeviation {
        if (!mineAsBig) {
            winning = false;
        }
        PromiseData data = promises.get(user);
        if (data == null) {
            StringBuilder builder = new StringBuilder();
            for (SenderReceiversPublicIdentity bidder : promises.keySet()) {
                new TradeGateKeeper(builder, bidder).invoke();
            }
            throw new IllegalOperationDeviation("Received bid comparison from " + user.obtainId() +
                    " but never received a bid commitment. Have these bidders: \n" + builder.toString());
        }
        data.defineTesting(mineAsBig);
    }

    public void addConcession(SenderReceiversPublicIdentity user) {
        PromiseData data = promises.get(user);
        data.concede();
        biddersStatus.addConcession(user);
    }

    public void addWinClaim(SenderReceiversPublicIdentity user, int promise) {
        PromiseData data = promises.get(user);
        data.claim(promise);
        biddersStatus.addWinClaim(user, promise);
    }

    /**
     * @param userId
     * @return the value of the claimed winning bid from userId, if there is one; null if not
     */
    public Integer takeExpectedWinningPromise(String userId) {
        for (SenderReceiversPublicIdentity user : promises.keySet()) {
            Integer data = obtainExpectedWinningPromiseFunction(userId, user);
            if (data != null) return data;
        }
        return null;
    }

    private Integer obtainExpectedWinningPromiseFunction(String userId, SenderReceiversPublicIdentity user) {
        if (user.obtainId().equals(userId)) {
            PromiseData data = promises.get(user);
            return data.obtainClaimingPromise();
        }
        return null;
    }


    public BiddersStatus fetchBiddersStatus() {
        return biddersStatus;
    }


    /**
     * This should only be used in the case where we discovered someone lied in their comparison message
     * and we have to figure out if we would have won had they not.  This shouldn't happen with normal use.
     *
     * @return the number of bids that are greater than mine
     */
    public int countPromisesAboveMine() {
        int count = 0;
        for (PromiseData data : promises.values()) {
            if (!data.mineAsBig) {
                count++;
            }
        }
        return count;
    }

    /**
     * @return is the revealedBid consistent with the comparison message conn sent for this auction?
     */
    public boolean isConsistentWithTesting(SenderReceiversPublicIdentity user, int revealedPromise) {
        if (myCommitment == null) {
            return true; // I didn't bid, so I have no info to check for consistency
        } else {
            return isConsistentWithTestingFunction(user, revealedPromise);
        }
    }

    private boolean isConsistentWithTestingFunction(SenderReceiversPublicIdentity user, int revealedPromise) {
        boolean claimedResult = (takeMyCommit().obtainPromise() >= revealedPromise);
        boolean recordedResult = wasMineAsBig(user);
        logger.info("claimedResult: mineAsBig? " + claimedResult);
        logger.info("recordedResult: mineAsBig? " + recordedResult);
        return claimedResult == recordedResult;
    }

    public boolean wasMineAsBig(SenderReceiversPublicIdentity user) {
        return promises.get(user).mineAsBig;
    }

    public boolean amIWinning() {
        // Note: we won't worry about the case where someone has bid but we haven't gotten their comparison yet
        // if they were the real winner, they will also claim such, and the seller will know the truth
        return myCommitment != null && winning;
    }

    public boolean didIPromise() {
        return myCommitment != null;
    }

    public void defineOver() {
        this.ended = true;
    }

    public boolean isOver() {
        return this.ended;
    }

    public boolean verifySeller(SenderReceiversPublicIdentity user) {
        return user.equals(seller);
    }

    public boolean verifyHighest(int claimedWinningPromise) {
        BiddersStatus status = fetchBiddersStatus();
        return status.verifyHighest(claimedWinningPromise);
    }

    public void setWinner(String winner, int winningPromise) {
        this.winner = winner;
        this.winningPromise = winningPromise;
    }

//////////////////////////  Class BidData ///////////////////////////////////////

    // class to hold any data regarding a bid from another user
    private class PromiseData {
        // class representing a user's claim (or concession) as winner of an auction
        private class ClaimOrConcession {
            private int promise = -1; // will only be set if conceded = false

            // constructor for claiming win
            private ClaimOrConcession(int promise) {
                this.promise = promise;
            }

            //constructor for concession
            private ClaimOrConcession() {
            }

            private boolean isConceded() {
                return (promise < 0);
            }
        }

        private BidCommitmentData commitment;
        private Boolean mineAsBig = null; // Is my bid as big as this one?  will be null until comparison has occurred
        private ClaimOrConcession claimStatus = null;

        private PromiseData(BidCommitmentData com) {
            commitment = com;
        }

        private BidCommitmentData obtainCommitment() {
            return commitment;
        }

        private void defineTesting(boolean mineAsBig) {
            this.mineAsBig = mineAsBig;
        }

        // mark this user as having conceded this auction
        private void claim(int promise) {
            this.claimStatus = new ClaimOrConcession(promise);
        }

        // mark this user as having claimed winnership in this auction
        private void concede() {
            this.claimStatus = new ClaimOrConcession(); // conceded claim
        }

        /**
         * @return claimed winner bid if user claimed to win, null otherwise
         */
        private Integer obtainClaimingPromise() {
            if (claimStatus == null || claimStatus.isConceded()) {
                return null;
            } else {
                return claimStatus.promise;
            }
        }
    }

    private class TradeGateKeeper {
        private StringBuilder builder;
        private SenderReceiversPublicIdentity bidder;

        public TradeGateKeeper(StringBuilder builder, SenderReceiversPublicIdentity bidder) {
            this.builder = builder;
            this.bidder = bidder;
        }

        public void invoke() {
            builder.append("have bidder " + bidder.toString());
            builder.append('\n');
        }
    }
}
