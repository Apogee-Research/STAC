package org.digitalapex.trade;

import org.digitalapex.trade.deviation.IllegalOperationRaiser;
import org.digitalapex.trade.deviation.RebidRaiser;
import org.digitalapex.trade.messagedata.PromiseData;
import org.digitalapex.trade.messagedata.BidConveyData;
import org.digitalapex.talkers.TalkersPublicIdentity;
import org.digitalapex.logging.Logger;
import org.digitalapex.logging.LoggerFactory;

import java.util.HashMap;

public class Selloff {
    private static final Logger logger = LoggerFactory.obtainLogger(Selloff.class);

    private String selloffId;
    private String selloffDescription;
    private HashMap<TalkersPublicIdentity, BidData> proposals = new HashMap<TalkersPublicIdentity, BidData>();
    private boolean winning = true; // is my bid as big as all I've seen so far
    private BidConveyData myCovenant;
    private TalkersPublicIdentity seller;
    private boolean sellerIsMe;
    private boolean ended = false; // has bidding period ended?
    private String winner;
    private int winningBid;
    private BiddersStatus biddersStatus = new BiddersStatus();// data useful for seller in choosing winner (and deciding whether he should wait to do so)

    public Selloff(String selloffId, TalkersPublicIdentity seller, String selloffDescription, boolean iAmSeller) {
        this.selloffId = selloffId;
        this.selloffDescription = selloffDescription;
        this.seller = seller;
        this.sellerIsMe = iAmSeller;
    }

    public String takeStatusString() {
        String status = "description: " + selloffDescription + "\n";
        if (amISeller()) {
            status += "I am the seller --" + seller.getId() + ". \n";
        } else {
            status += "Seller is " + seller.getId() + ".\n";
        }
        if (!ended) {
            status += "Status: open. \n";
        } else if (winner != null) {
            status += "Status: won by " + winner + " for $" + winningBid + ". \n";
        } else {
            status += "Status: bidding ended, but winner not yet announced. \n";
        }
        if (myCovenant == null) {
            status += "I did not bid.\n";
        } else {
            status += "I bid $" + myCovenant.fetchBid() + ".\n";
        }
        return status;
    }

    public boolean amISeller() {
        return sellerIsMe;
    }


    public void addCovenant(TalkersPublicIdentity user, PromiseData covenant) throws RebidRaiser {
        BidData data;
        if (!proposals.containsKey(user)) {
            data = new BidData(covenant);
            proposals.put(user, data);
            biddersStatus.addBidder(user);
        } else {
            addCovenantGuide(user);
        }
    }

    private void addCovenantGuide(TalkersPublicIdentity user) throws RebidRaiser {
        throw new RebidRaiser("User " + user + " has sent more than one bid for auction " + selloffId);
    }

    public void recordMyCommit(BidConveyData covenant, TalkersPublicIdentity myIdentity) throws IllegalOperationRaiser, RebidRaiser {

        myCovenant = covenant;
        biddersStatus.addBidder(myIdentity);
        addCovenant(myIdentity, covenant.grabCovenantData(myIdentity));
        addObservation(myIdentity, true); // my bid is as big as my bid

    }

    public BidConveyData getMyCommit() {
        return myCovenant;
    }

    // get user's bid commitment on this auction
    public PromiseData takeBidCovenant(TalkersPublicIdentity user) {
        return proposals.get(user).fetchCovenant();
    }

    public void removeBid(TalkersPublicIdentity user) {
        proposals.remove(user);
        biddersStatus.removeBidder(user);
    }


    public void addObservation(TalkersPublicIdentity user, boolean mineAsBig) throws IllegalOperationRaiser {
        if (!mineAsBig) {
            winning = false;
        }
        BidData data = proposals.get(user);
        if (data == null) {
            addObservationFunction(user);
        }
        data.setObservation(mineAsBig);
    }

    private void addObservationFunction(TalkersPublicIdentity user) throws IllegalOperationRaiser {
        StringBuilder builder = new StringBuilder();
        for (TalkersPublicIdentity bidder : proposals.keySet()) {
            builder.append("have bidder " + bidder.toString());
            builder.append('\n');
        }
        throw new IllegalOperationRaiser("Received bid comparison from " + user.getId() +
                " but never received a bid commitment. Have these bidders: \n" + builder.toString());
    }

    public void addConcession(TalkersPublicIdentity user) {
        BidData data = proposals.get(user);
        data.concede();
        biddersStatus.addConcession(user);
    }

    public void addWinClaim(TalkersPublicIdentity user, int bid) {
        BidData data = proposals.get(user);
        data.claim(bid);
        biddersStatus.addWinClaim(user, bid);
    }

    /**
     * @param userId
     * @return the value of the claimed winning bid from userId, if there is one; null if not
     */
    public Integer pullExpectedWinningBid(String userId) {
        for (TalkersPublicIdentity user : proposals.keySet()) {
            Integer data = obtainExpectedWinningBidTarget(userId, user);
            if (data != null) return data;
        }
        return null;
    }

    private Integer obtainExpectedWinningBidTarget(String userId, TalkersPublicIdentity user) {
        if (user.getId().equals(userId)) {
            BidData data = proposals.get(user);
            return data.pullClaimingBid();
        }
        return null;
    }


    public BiddersStatus grabBiddersStatus() {
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
        for (BidData data : proposals.values()) {
            if (!data.mineAsBig) {
                count++;
            }
        }
        return count;
    }

    /**
     * @return is the revealedBid consistent with the comparison message conn sent for this auction?
     */
    public boolean isConsistentWithObservation(TalkersPublicIdentity user, int revealedBid) {
        if (myCovenant == null) {
            return true; // I didn't bid, so I have no info to check for consistency
        } else {
            boolean claimedResult = (getMyCommit().fetchBid() >= revealedBid);
            boolean recordedResult = wasMineAsBig(user);
            logger.info("claimedResult: mineAsBig? " + claimedResult);
            logger.info("recordedResult: mineAsBig? " + recordedResult);
            return claimedResult == recordedResult;
        }
    }

    public boolean wasMineAsBig(TalkersPublicIdentity user) {
        return proposals.get(user).mineAsBig;
    }

    public boolean amIWinning() {
        // Note: we won't worry about the case where someone has bid but we haven't gotten their comparison yet
        // if they were the real winner, they will also claim such, and the seller will know the truth
        return myCovenant != null && winning;
    }

    public boolean didIBid() {
        return myCovenant != null;
    }

    public void assignOver() {
        this.ended = true;
    }

    public boolean isOver() {
        return this.ended;
    }

    public boolean verifySeller(TalkersPublicIdentity user) {
        return user.equals(seller);
    }

    public boolean verifyHighest(int claimedWinningBid) {
        BiddersStatus status = grabBiddersStatus();
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

        private PromiseData covenant;
        private Boolean mineAsBig = null; // Is my bid as big as this one?  will be null until comparison has occurred
        private ClaimOrConcession claimStatus = null;

        private BidData(PromiseData com) {
            covenant = com;
        }

        private PromiseData fetchCovenant() {
            return covenant;
        }

        private void setObservation(boolean mineAsBig) {
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
        private Integer pullClaimingBid() {
            if (claimStatus == null || claimStatus.isConceded()) {
                return null;
            } else {
                return claimStatus.bid;
            }
        }
    }
}
