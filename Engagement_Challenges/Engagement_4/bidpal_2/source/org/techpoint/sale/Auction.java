package org.techpoint.sale;

import org.techpoint.sale.exception.IllegalOperationRaiser;
import org.techpoint.sale.exception.RebidRaiser;
import org.techpoint.sale.messagedata.BidCommitmentData;
import org.techpoint.sale.messagedata.ProposalReportData;
import org.techpoint.communications.CommsPublicIdentity;
import org.techpoint.logging.Logger;
import org.techpoint.logging.LoggerFactory;

import java.util.HashMap;

public class Auction {
    private static final Logger logger = LoggerFactory.fetchLogger(Auction.class);

    private String auctionId;
    private String auctionDescription;
    private HashMap<CommsPublicIdentity, ProposalData> proposals = new HashMap<CommsPublicIdentity, ProposalData>();
    private boolean winning = true; // is my bid as big as all I've seen so far
    private ProposalReportData myCommitment;
    private CommsPublicIdentity seller;
    private boolean sellerIsMe;
    private boolean ended = false; // has bidding period ended?
    private String winner;
    private int winningProposal;
    private BiddersStatus biddersStatus = new BiddersStatus();// data useful for seller in choosing winner (and deciding whether he should wait to do so)

    public Auction(String auctionId, CommsPublicIdentity seller, String auctionDescription, boolean iAmSeller) {
        this.auctionId = auctionId;
        this.auctionDescription = auctionDescription;
        this.seller = seller;
        this.sellerIsMe = iAmSeller;
    }

    public String takeStatusString() {
        String status = "description: " + auctionDescription + "\n";
        if (amISeller()) {
            status += "I am the seller --" + seller.grabId() + ". \n";
        } else {
            status += "Seller is " + seller.grabId() + ".\n";
        }
        if (!ended) {
            status += "Status: open. \n";
        } else if (winner != null) {
            status += "Status: won by " + winner + " for $" + winningProposal + ". \n";
        } else {
            status += "Status: bidding ended, but winner not yet announced. \n";
        }
        if (myCommitment == null) {
            status += "I did not bid.\n";
        } else {
            status += "I bid $" + myCommitment.getProposal() + ".\n";
        }
        return status;
    }

    public boolean amISeller() {
        return sellerIsMe;
    }


    public void addCommitment(CommsPublicIdentity member, BidCommitmentData commitment) throws RebidRaiser {
        ProposalData data;
        if (!proposals.containsKey(member)) {
            data = new ProposalData(commitment);
            proposals.put(member, data);
            biddersStatus.addBidder(member);
        } else {
            addCommitmentHelper(member);
        }
    }

    private void addCommitmentHelper(CommsPublicIdentity member) throws RebidRaiser {
        throw new RebidRaiser("User " + member + " has sent more than one bid for auction " + auctionId);
    }

    public void recordMyCommit(ProposalReportData commitment, CommsPublicIdentity myIdentity) throws IllegalOperationRaiser, RebidRaiser {

        myCommitment = commitment;
        biddersStatus.addBidder(myIdentity);
        addCommitment(myIdentity, commitment.getCommitmentData(myIdentity));
        addDistinguisher(myIdentity, true); // my bid is as big as my bid

    }

    public ProposalReportData pullMyCommit() {
        return myCommitment;
    }

    // get user's bid commitment on this auction
    public BidCommitmentData fetchProposalCommitment(CommsPublicIdentity member) {
        return proposals.get(member).getCommitment();
    }

    public void removeProposal(CommsPublicIdentity member) {
        proposals.remove(member);
        biddersStatus.removeBidder(member);
    }


    public void addDistinguisher(CommsPublicIdentity member, boolean mineAsBig) throws IllegalOperationRaiser {
        if (!mineAsBig) {
            addDistinguisherService();
        }
        ProposalData data = proposals.get(member);
        if (data == null) {
            addDistinguisherUtility(member);
        }
        data.defineDistinguisher(mineAsBig);
    }

    private void addDistinguisherUtility(CommsPublicIdentity member) throws IllegalOperationRaiser {
        StringBuilder builder = new StringBuilder();
        for (CommsPublicIdentity bidder : proposals.keySet()) {
            builder.append("have bidder " + bidder.toString());
            builder.append('\n');
        }
        throw new IllegalOperationRaiser("Received bid comparison from " + member.grabId() +
                " but never received a bid commitment. Have these bidders: \n" + builder.toString());
    }

    private void addDistinguisherService() {
        winning = false;
    }

    public void addConcession(CommsPublicIdentity member) {
        ProposalData data = proposals.get(member);
        data.concede();
        biddersStatus.addConcession(member);
    }

    public void addWinClaim(CommsPublicIdentity member, int proposal) {
        ProposalData data = proposals.get(member);
        data.claim(proposal);
        biddersStatus.addWinClaim(member, proposal);
    }

    /**
     * @param memberId
     * @return the value of the claimed winning bid from userId, if there is one; null if not
     */
    public Integer grabExpectedWinningProposal(String memberId) {
        for (CommsPublicIdentity member : proposals.keySet()) {
            if (member.grabId().equals(memberId)) {
                ProposalData data = proposals.get(member);
                return data.getClaimingProposal();
            }
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
    public int countProposalsAboveMine() {
        int count = 0;
        for (ProposalData data : proposals.values()) {
            if (!data.mineAsBig) {
                count++;
            }
        }
        return count;
    }

    /**
     * @return is the revealedBid consistent with the comparison message conn sent for this auction?
     */
    public boolean isConsistentWithDistinguisher(CommsPublicIdentity member, int revealedProposal) {
        if (myCommitment == null) {
            return true; // I didn't bid, so I have no info to check for consistency
        } else {
            boolean claimedResult = (pullMyCommit().getProposal() >= revealedProposal);
            boolean recordedResult = wasMineAsBig(member);
            logger.info("claimedResult: mineAsBig? " + claimedResult);
            logger.info("recordedResult: mineAsBig? " + recordedResult);
            return claimedResult == recordedResult;
        }
    }

    public boolean wasMineAsBig(CommsPublicIdentity member) {
        return proposals.get(member).mineAsBig;
    }

    public boolean amIWinning() {
        // Note: we won't worry about the case where someone has bid but we haven't gotten their comparison yet
        // if they were the real winner, they will also claim such, and the seller will know the truth
        return myCommitment != null && winning;
    }

    public boolean didIProposal() {
        return myCommitment != null;
    }

    public void fixOver() {
        this.ended = true;
    }

    public boolean isOver() {
        return this.ended;
    }

    public boolean verifySeller(CommsPublicIdentity member) {
        return member.equals(seller);
    }

    public boolean verifyHighest(int claimedWinningProposal) {
        BiddersStatus status = pullBiddersStatus();
        return status.verifyHighest(claimedWinningProposal);
    }

    public void defineWinner(String winner, int winningProposal) {
        this.winner = winner;
        this.winningProposal = winningProposal;
    }

//////////////////////////  Class BidData ///////////////////////////////////////

    // class to hold any data regarding a bid from another user
    private class ProposalData {
        // class representing a user's claim (or concession) as winner of an auction
        private class ClaimOrConcession {
            private int proposal = -1; // will only be set if conceded = false

            // constructor for claiming win
            private ClaimOrConcession(int proposal) {
                this.proposal = proposal;
            }

            //constructor for concession
            private ClaimOrConcession() {
            }

            private boolean isConceded() {
                return (proposal < 0);
            }
        }

        private BidCommitmentData commitment;
        private Boolean mineAsBig = null; // Is my bid as big as this one?  will be null until comparison has occurred
        private ClaimOrConcession claimStatus = null;

        private ProposalData(BidCommitmentData com) {
            commitment = com;
        }

        private BidCommitmentData getCommitment() {
            return commitment;
        }

        private void defineDistinguisher(boolean mineAsBig) {
            this.mineAsBig = mineAsBig;
        }

        // mark this user as having conceded this auction
        private void claim(int proposal) {
            this.claimStatus = new ClaimOrConcession(proposal);
        }

        // mark this user as having claimed winnership in this auction
        private void concede() {
            this.claimStatus = new ClaimOrConcession(); // conceded claim
        }

        /**
         * @return claimed winner bid if user claimed to win, null otherwise
         */
        private Integer getClaimingProposal() {
            if (claimStatus == null || claimStatus.isConceded()) {
                return null;
            } else {
                return claimStatus.proposal;
            }
        }
    }
}
