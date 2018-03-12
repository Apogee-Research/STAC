package edu.networkcusp.buyOp;

import edu.networkcusp.buyOp.bad.IllegalOperationRaiser;
import edu.networkcusp.buyOp.bad.RebidRaiser;
import edu.networkcusp.buyOp.messagedata.PromiseData;
import edu.networkcusp.buyOp.messagedata.OfferConveyData;
import edu.networkcusp.senderReceivers.ProtocolsPublicIdentity;
import edu.networkcusp.logging.Logger;
import edu.networkcusp.logging.LoggerFactory;

import java.util.HashMap;

public class Auction {
    private static final Logger logger = LoggerFactory.pullLogger(Auction.class);

    private String auctionId;
    private String auctionDescription;
    private HashMap<ProtocolsPublicIdentity, OfferData> proposals = new HashMap<ProtocolsPublicIdentity, OfferData>();
    private boolean winning = true; // is my bid as big as all I've seen so far
    private OfferConveyData myContract;
    private ProtocolsPublicIdentity seller;
    private boolean sellerIsMe;
    private boolean ended = false; // has bidding period ended?
    private String winner;
    private int winningOffer;
    private BiddersStatus biddersStatus = new BiddersStatusBuilder().createBiddersStatus();// data useful for seller in choosing winner (and deciding whether he should wait to do so)

    public Auction(String auctionId, ProtocolsPublicIdentity seller, String auctionDescription, boolean iAmSeller) {
        this.auctionId = auctionId;
        this.auctionDescription = auctionDescription;
        this.seller = seller;
        this.sellerIsMe = iAmSeller;
    }

    public String grabStatusString() {
        String status = "description: " + auctionDescription + "\n";
        if (amISeller()) {
            status += "I am the seller --" + seller.fetchId() + ". \n";
        } else {
            status += "Seller is " + seller.fetchId() + ".\n";
        }
        if (!ended) {
            status += "Status: open. \n";
        } else if (winner != null) {
            status += "Status: won by " + winner + " for $" + winningOffer + ". \n";
        } else {
            status += "Status: bidding ended, but winner not yet announced. \n";
        }
        if (myContract == null) {
            status += "I did not bid.\n";
        } else {
            status += "I bid $" + myContract.pullOffer() + ".\n";
        }
        return status;
    }

    public boolean amISeller() {
        return sellerIsMe;
    }


    public void addContract(ProtocolsPublicIdentity customer, PromiseData contract) throws RebidRaiser {
        OfferData data;
        if (!proposals.containsKey(customer)) {
            data = new OfferData(contract);
            proposals.put(customer, data);
            biddersStatus.addBidder(customer);
        } else {
            new AuctionFunction(customer).invoke();
        }
    }

    public void recordMyCommit(OfferConveyData contract, ProtocolsPublicIdentity myIdentity) throws IllegalOperationRaiser, RebidRaiser {

        myContract = contract;
        biddersStatus.addBidder(myIdentity);
        addContract(myIdentity, contract.getContractData(myIdentity));
        addTesting(myIdentity, true); // my bid is as big as my bid

    }

    public OfferConveyData grabMyCommit() {
        return myContract;
    }

    // get user's bid commitment on this auction
    public PromiseData grabOfferContract(ProtocolsPublicIdentity customer) {
        return proposals.get(customer).grabContract();
    }

    public void removeOffer(ProtocolsPublicIdentity customer) {
        proposals.remove(customer);
        biddersStatus.removeBidder(customer);
    }


    public void addTesting(ProtocolsPublicIdentity customer, boolean mineAsBig) throws IllegalOperationRaiser {
        if (!mineAsBig) {
            winning = false;
        }
        OfferData data = proposals.get(customer);
        if (data == null) {
            addTestingHelper(customer);
        }
        data.assignTesting(mineAsBig);
    }

    private void addTestingHelper(ProtocolsPublicIdentity customer) throws IllegalOperationRaiser {
        StringBuilder builder = new StringBuilder();
        for (ProtocolsPublicIdentity bidder : proposals.keySet()) {
            addTestingHelperAid(builder, bidder);
        }
        throw new IllegalOperationRaiser("Received bid comparison from " + customer.fetchId() +
                " but never received a bid commitment. Have these bidders: \n" + builder.toString());
    }

    private void addTestingHelperAid(StringBuilder builder, ProtocolsPublicIdentity bidder) {
        builder.append("have bidder " + bidder.toString());
        builder.append('\n');
    }

    public void addConcession(ProtocolsPublicIdentity customer) {
        OfferData data = proposals.get(customer);
        data.concede();
        biddersStatus.addConcession(customer);
    }

    public void addWinClaim(ProtocolsPublicIdentity customer, int offer) {
        OfferData data = proposals.get(customer);
        data.claim(offer);
        biddersStatus.addWinClaim(customer, offer);
    }

    /**
     * @param customerId
     * @return the value of the claimed winning bid from userId, if there is one; null if not
     */
    public Integer grabExpectedWinningOffer(String customerId) {
        for (ProtocolsPublicIdentity customer : proposals.keySet()) {
            Integer data = getExpectedWinningOfferHome(customerId, customer);
            if (data != null) return data;
        }
        return null;
    }

    private Integer getExpectedWinningOfferHome(String customerId, ProtocolsPublicIdentity customer) {
        if (customer.fetchId().equals(customerId)) {
            OfferData data = proposals.get(customer);
            return data.grabClaimingOffer();
        }
        return null;
    }


    public BiddersStatus pullBiddersStatus() {
        return biddersStatus;
    }


    /**
     * This should only be used in the case where we discovered someone lied in their comparison message
     * and we have to figure out if we would have won had they not.  This shouldn't happen with normal use.
     *
     * @return the number of bids that are greater than mine
     */
    public int countOffersAboveMine() {
        int count = 0;
        for (OfferData data : proposals.values()) {
            if (!data.mineAsBig) {
                count++;
            }
        }
        return count;
    }

    /**
     * @return is the revealedBid consistent with the comparison message conn sent for this auction?
     */
    public boolean isConsistentWithTesting(ProtocolsPublicIdentity customer, int revealedOffer) {
        if (myContract == null) {
            return true; // I didn't bid, so I have no info to check for consistency
        } else {
            boolean claimedResult = (grabMyCommit().pullOffer() >= revealedOffer);
            boolean recordedResult = wasMineAsBig(customer);
            logger.info("claimedResult: mineAsBig? " + claimedResult);
            logger.info("recordedResult: mineAsBig? " + recordedResult);
            return claimedResult == recordedResult;
        }
    }

    public boolean wasMineAsBig(ProtocolsPublicIdentity customer) {
        return proposals.get(customer).mineAsBig;
    }

    public boolean amIWinning() {
        // Note: we won't worry about the case where someone has bid but we haven't gotten their comparison yet
        // if they were the real winner, they will also claim such, and the seller will know the truth
        return myContract != null && winning;
    }

    public boolean didIOffer() {
        return myContract != null;
    }

    public void defineOver() {
        this.ended = true;
    }

    public boolean isOver() {
        return this.ended;
    }

    public boolean verifySeller(ProtocolsPublicIdentity customer) {
        return customer.equals(seller);
    }

    public boolean verifyHighest(int claimedWinningOffer) {
        BiddersStatus status = pullBiddersStatus();
        return status.verifyHighest(claimedWinningOffer);
    }

    public void setWinner(String winner, int winningOffer) {
        this.winner = winner;
        this.winningOffer = winningOffer;
    }

//////////////////////////  Class BidData ///////////////////////////////////////

    // class to hold any data regarding a bid from another user
    private class OfferData {
        // class representing a user's claim (or concession) as winner of an auction
        private class ClaimOrConcession {
            private int offer = -1; // will only be set if conceded = false

            // constructor for claiming win
            private ClaimOrConcession(int offer) {
                this.offer = offer;
            }

            //constructor for concession
            private ClaimOrConcession() {
            }

            private boolean isConceded() {
                return (offer < 0);
            }
        }

        private PromiseData contract;
        private Boolean mineAsBig = null; // Is my bid as big as this one?  will be null until comparison has occurred
        private ClaimOrConcession claimStatus = null;

        private OfferData(PromiseData com) {
            contract = com;
        }

        private PromiseData grabContract() {
            return contract;
        }

        private void assignTesting(boolean mineAsBig) {
            this.mineAsBig = mineAsBig;
        }

        // mark this user as having conceded this auction
        private void claim(int offer) {
            this.claimStatus = new ClaimOrConcession(offer);
        }

        // mark this user as having claimed winnership in this auction
        private void concede() {
            this.claimStatus = new ClaimOrConcession(); // conceded claim
        }

        /**
         * @return claimed winner bid if user claimed to win, null otherwise
         */
        private Integer grabClaimingOffer() {
            if (claimStatus == null || claimStatus.isConceded()) {
                return null;
            } else {
                return claimStatus.offer;
            }
        }
    }

    private class AuctionFunction {
        private ProtocolsPublicIdentity customer;

        public AuctionFunction(ProtocolsPublicIdentity customer) {
            this.customer = customer;
        }

        public void invoke() throws RebidRaiser {
            throw new RebidRaiser("User " + customer + " has sent more than one bid for auction " + auctionId);
        }
    }
}
