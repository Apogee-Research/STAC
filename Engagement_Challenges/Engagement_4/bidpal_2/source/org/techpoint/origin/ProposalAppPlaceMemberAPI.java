package org.techpoint.origin;

import org.techpoint.sale.AuctionMemberAPI;
import org.techpoint.sale.messagedata.AuctionMessageData;
import org.techpoint.sale.messagedata.BidCommitmentData;
import org.techpoint.sale.messagedata.BidComparisonData;
import org.techpoint.sale.messagedata.ProposalReportData;
import org.techpoint.communications.CommsPublicIdentity;

public class ProposalAppPlaceMemberAPI implements AuctionMemberAPI {
    private String username;

    public ProposalAppPlaceMemberAPI(String username) {
        this.username = username;
    }

    public void newAuction(CommsPublicIdentity member, AuctionMessageData.AuctionStart data) {
        System.out.println(username + " received auction start announcement " + data.getAuctionId() +": "  + data.description);
    }

    public void proposalCommitmentReceived(CommsPublicIdentity member, BidCommitmentData data) {
        System.out.println(username + " received a bid commitment from " + member.grabId());
    }

    public void proposalDistinguisherReceived(CommsPublicIdentity member, BidComparisonData data) {
        System.out.println(username + " received a bid comparison from " + member.grabId());
        System.out.println(member.grabId() + " bid on " + data.getAuctionId());
    }

    public void proposalReceiptReceived(CommsPublicIdentity member, AuctionMessageData.ProposalReceipt data){
        System.out.println(username + " received a bid receipt from " + member.grabId() + " for auction " + data.getAuctionId());
    }

    public void biddingEnded(CommsPublicIdentity member, AuctionMessageData.BiddingOver data) {
        System.out.println(username + " received bidding closed announcement " + data.getAuctionId());
    }

    public void concessionReceived(CommsPublicIdentity member, AuctionMessageData.Concession data) {
        System.out.println(username + " received an auction concession from " + member.grabId()
                + " for " + data.getAuctionId());
    }

    public void winClaimReceived(CommsPublicIdentity member, ProposalReportData data) {
        System.out.println(username + " received a win claim from " + member.grabId()
                + " for " + data.getAuctionId());
    }

    public void auctionOver(CommsPublicIdentity member, AuctionMessageData.AuctionEnd data) {
        System.out.println(username + " received end of auction announcement " + data.getAuctionId());
    }

}
