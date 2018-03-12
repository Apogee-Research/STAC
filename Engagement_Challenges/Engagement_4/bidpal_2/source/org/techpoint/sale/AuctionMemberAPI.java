package org.techpoint.sale;

import org.techpoint.sale.messagedata.AuctionMessageData;
import org.techpoint.sale.messagedata.BidCommitmentData;
import org.techpoint.sale.messagedata.BidComparisonData;
import org.techpoint.sale.messagedata.ProposalReportData;
import org.techpoint.communications.CommsPublicIdentity;

/**
 * Interface for users of the BidPal auction protocol.  
 * This is to communicate with the user (e.g. the bidder or seller), not for making the auction happen.
 * I.e., if you'd like to print a message to the screen or initiate some other action when a certain auction event occurs, 
 * this is the place to do it.  In some cases, it could easily be appropriate for these methods to do nothing at all. 
 */
public interface AuctionMemberAPI {
	void newAuction(CommsPublicIdentity member, AuctionMessageData.AuctionStart data);
	void proposalCommitmentReceived(CommsPublicIdentity member, BidCommitmentData data);
	void proposalDistinguisherReceived(CommsPublicIdentity member, BidComparisonData data);
	void proposalReceiptReceived(CommsPublicIdentity member, AuctionMessageData.ProposalReceipt data);
	void biddingEnded(CommsPublicIdentity member, AuctionMessageData.BiddingOver data);
	void concessionReceived(CommsPublicIdentity member, AuctionMessageData.Concession data);
	void winClaimReceived(CommsPublicIdentity member, ProposalReportData data);
	void auctionOver(CommsPublicIdentity member, AuctionMessageData.AuctionEnd data);
}

