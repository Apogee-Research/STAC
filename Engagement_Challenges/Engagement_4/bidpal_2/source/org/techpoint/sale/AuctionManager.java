package org.techpoint.sale;

import org.techpoint.sale.messagedata.AuctionMessageData;
import org.techpoint.sale.messagedata.AuctionMessageData.AuctionEnd;
import org.techpoint.sale.messagedata.AuctionMessageData.AuctionStart;
import org.techpoint.sale.messagedata.AuctionMessageData.ProposalReceipt;
import org.techpoint.sale.messagedata.AuctionMessageData.BiddingOver;
import org.techpoint.sale.messagedata.AuctionMessageData.Concession;
import org.techpoint.sale.messagedata.AuctionSerializer;
import org.techpoint.sale.messagedata.BidCommitmentData;
import org.techpoint.sale.messagedata.BidComparisonData;
import org.techpoint.sale.messagedata.ProposalReportData;
import org.techpoint.communications.CommsRaiser;
import org.techpoint.communications.CommsIdentity;
import org.techpoint.communications.CommsPublicIdentity;
import org.techpoint.communications.Communicator;


/**
 * CommsHandler (and Communicator) for secret bidding.
 *
 */
public class AuctionManager {

    private AuctionDirector director;

    private final CommsIdentity identity; // identity of this user

    private AuctionSerializer serializer;

    private AuctionMemberAPI memberAPI;

    public AuctionManager(AuctionDirector director, Communicator communicator, AuctionMemberAPI memberAPI, AuctionSerializer serializer, int port, CommsIdentity identity, int maxProposal) {
        this.serializer = serializer;
        this.identity = identity;
        this.memberAPI = memberAPI;
        this.director = director;
    }


    public synchronized void handle(CommsPublicIdentity member, byte[] msgData) throws CommsRaiser {
        try {
            AuctionMessageData data = serializer.deserialize(msgData);
            String auctionId = data.getAuctionId();
            switch (data.type) {
                case BID_COMMITMENT:
                    memberAPI.proposalCommitmentReceived(member, (BidCommitmentData) data);
                    director.processCommit(member, (BidCommitmentData) data);
                    break;
                case BID_COMPARISON:
                    memberAPI.proposalDistinguisherReceived(member, (BidComparisonData) data);
                    director.processDistinguisher(member, (BidComparisonData) data);
                    break;
                case BID_RECEIPT:
                    memberAPI.proposalReceiptReceived(member, (ProposalReceipt) data);
                    // no action required
                    break;
                case AUCTION_START:
                    AuctionStart startData = (AuctionStart) data;
                    memberAPI.newAuction(member, startData);
                    director.processNewAuction(member, auctionId, startData.description);
                    break;
                case BIDDING_OVER:
                    memberAPI.biddingEnded(member, (BiddingOver) data);
                    director.biddingOver(member, auctionId);
                    break;
                case AUCTION_END:
                    AuctionEnd endData = (AuctionEnd) data;
                    memberAPI.auctionOver(member, endData);
                    director.processAuctionEnd(member, auctionId, endData.winner, endData.winningProposal);
                    break;
                case CLAIM_WIN:
                    ProposalReportData reportData = (ProposalReportData) data;
                    memberAPI.winClaimReceived(member, reportData);
                    director.processWinClaim(member, auctionId, reportData);
                    break;
                case CONCESSION:
                    memberAPI.concessionReceived(member, (Concession) data);
                    director.processConcession(member, auctionId);
                    break;
                default:
                    System.err.println(identity.takeId() + " received an unknown message " + data.type);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void addMember(CommsPublicIdentity member) {
        director.addMember(member);
    }

    public void removeMember(CommsPublicIdentity member) {
        director.removeMember(member);
    }
}