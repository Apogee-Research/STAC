package com.virtualpoint.barter;

import com.virtualpoint.barter.failure.IllegalOperationTrouble;
import com.virtualpoint.barter.failure.RebidTrouble;
import com.virtualpoint.barter.messagedata.OfferSubmission;
import com.virtualpoint.barter.messagedata.BidConveyData;
import com.virtualpoint.talkers.DialogsPublicIdentity;
import com.virtualpoint.logging.Logger;
import com.virtualpoint.logging.LoggerFactory;

import java.util.HashMap;

public class Barter {
    private static final Logger logger = LoggerFactory.fetchLogger(Barter.class);

    private String barterId;
    private String barterDescription;
    private HashMap<DialogsPublicIdentity, BidData> offers = new HashMap<DialogsPublicIdentity, BidData>();
    private boolean winning = true; // is my bid as big as all I've seen so far
    private BidConveyData myContract;
    private DialogsPublicIdentity seller;
    private boolean sellerIsMe;
    private boolean ended = false; // has bidding period ended?
    private String winner;
    private int winningBid;
    private BiddersStatus biddersStatus = new BiddersStatus();// data useful for seller in choosing winner (and deciding whether he should wait to do so)

    public Barter(String barterId, DialogsPublicIdentity seller, String barterDescription, boolean iAmSeller) {
        this.barterId = barterId;
        this.barterDescription = barterDescription;
        this.seller = seller;
        this.sellerIsMe = iAmSeller;
    }

    public String fetchStatusString() {
        String status = "description: " + barterDescription + "\n";
        if (amISeller()) {
            status += "I am the seller --" + seller.obtainId() + ". \n";
        } else {
            status += "Seller is " + seller.obtainId() + ".\n";
        }
        if (!ended) {
            status += "Status: open. \n";
        } else if (winner != null) {
            status += "Status: won by " + winner + " for $" + winningBid + ". \n";
        } else {
            status += "Status: bidding ended, but winner not yet announced. \n";
        }
        if (myContract == null) {
            status += "I did not bid.\n";
        } else {
            status += "I bid $" + myContract.takeBid() + ".\n";
        }
        return status;
    }

    public boolean amISeller() {
        return sellerIsMe;
    }


    public void addContract(DialogsPublicIdentity user, OfferSubmission contract) throws RebidTrouble {
        BidData data;
        if (!offers.containsKey(user)) {
            data = new BidData(contract);
            offers.put(user, data);
            biddersStatus.addBidder(user);
        } else {
            throw new RebidTrouble("User " + user + " has sent more than one bid for auction " + barterId);
        }
    }

    public void recordMyCommit(BidConveyData contract, DialogsPublicIdentity myIdentity) throws IllegalOperationTrouble, RebidTrouble {

        myContract = contract;
        biddersStatus.addBidder(myIdentity);
        addContract(myIdentity, contract.fetchContractData(myIdentity));
        addTesting(myIdentity, true); // my bid is as big as my bid

    }

    public BidConveyData obtainMyCommit() {
        return myContract;
    }

    // get user's bid commitment on this auction
    public OfferSubmission takeBidContract(DialogsPublicIdentity user) {
        return offers.get(user).pullContract();
    }

    public void removeBid(DialogsPublicIdentity user) {
        offers.remove(user);
        biddersStatus.removeBidder(user);
    }


    public void addTesting(DialogsPublicIdentity user, boolean mineAsBig) throws IllegalOperationTrouble {
        if (!mineAsBig) {
            winning = false;
        }
        BidData data = offers.get(user);
        if (data == null) {
            StringBuilder builder = new StringBuilder();
            for (DialogsPublicIdentity bidder : offers.keySet()) {
                builder.append("have bidder " + bidder.toString());
                builder.append('\n');
            }
            throw new IllegalOperationTrouble("Received bid comparison from " + user.obtainId() +
                    " but never received a bid commitment. Have these bidders: \n" + builder.toString());
        }
        data.defineTesting(mineAsBig);
    }

    public void addConcession(DialogsPublicIdentity user) {
        BidData data = offers.get(user);
        data.concede();
        biddersStatus.addConcession(user);
    }

    public void addWinClaim(DialogsPublicIdentity user, int bid) {
        BidData data = offers.get(user);
        data.claim(bid);
        biddersStatus.addWinClaim(user, bid);
    }

    /**
     * @param userId
     * @return the value of the claimed winning bid from userId, if there is one; null if not
     */
    public Integer fetchExpectedWinningBid(String userId) {
        for (DialogsPublicIdentity user : offers.keySet()) {
            if (user.obtainId().equals(userId)) {
                return takeExpectedWinningBidHelp(user);
            }
        }
        return null;
    }

    private Integer takeExpectedWinningBidHelp(DialogsPublicIdentity user) {
        BidData data = offers.get(user);
        return data.getClaimingBid();
    }


    public BiddersStatus takeBiddersStatus() {
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
    public boolean isConsistentWithTesting(DialogsPublicIdentity user, int revealedBid) {
        if (myContract == null) {
            return true; // I didn't bid, so I have no info to check for consistency
        } else {
            boolean claimedResult = (obtainMyCommit().takeBid() >= revealedBid);
            boolean recordedResult = wasMineAsBig(user);
            logger.info("claimedResult: mineAsBig? " + claimedResult);
            logger.info("recordedResult: mineAsBig? " + recordedResult);
            return claimedResult == recordedResult;
        }
    }

    public boolean wasMineAsBig(DialogsPublicIdentity user) {
        return offers.get(user).mineAsBig;
    }

    public boolean amIWinning() {
        // Note: we won't worry about the case where someone has bid but we haven't gotten their comparison yet
        // if they were the real winner, they will also claim such, and the seller will know the truth
        return myContract != null && winning;
    }

    public boolean didIBid() {
        return myContract != null;
    }

    public void setOver() {
        this.ended = true;
    }

    public boolean isOver() {
        return this.ended;
    }

    public boolean verifySeller(DialogsPublicIdentity user) {
        return user.equals(seller);
    }

    public boolean verifyHighest(int claimedWinningBid) {
        BiddersStatus status = takeBiddersStatus();
        return status.verifyHighest(claimedWinningBid);
    }

    public void setWinner(String winner, int winningBid) {
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

        private OfferSubmission contract;
        private Boolean mineAsBig = null; // Is my bid as big as this one?  will be null until comparison has occurred
        private ClaimOrConcession claimStatus = null;

        private BidData(OfferSubmission com) {
            contract = com;
        }

        private OfferSubmission pullContract() {
            return contract;
        }

        private void defineTesting(boolean mineAsBig) {
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
}
