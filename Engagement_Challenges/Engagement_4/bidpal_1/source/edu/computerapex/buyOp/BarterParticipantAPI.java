package edu.computerapex.buyOp;

import edu.computerapex.buyOp.messagedata.BarterMessageData;
import edu.computerapex.buyOp.messagedata.BidCommitmentData;
import edu.computerapex.buyOp.messagedata.ExchangeData;
import edu.computerapex.buyOp.messagedata.BidDivulgeData;
import edu.computerapex.dialogs.CommunicationsPublicIdentity;

/**
 * Interface for users of the BidPal auction protocol.  
 * This is to communicate with the user (e.g. the bidder or seller), not for making the auction happen.
 * I.e., if you'd like to print a message to the screen or initiate some other action when a certain auction event occurs, 
 * this is the place to do it.  In some cases, it could easily be appropriate for these methods to do nothing at all. 
 */
public interface BarterParticipantAPI {
	void newBarter(CommunicationsPublicIdentity participant, BarterMessageData.BarterStart data);
	void bidCommitmentReceived(CommunicationsPublicIdentity participant, BidCommitmentData data);
	void bidMeasurementReceived(CommunicationsPublicIdentity participant, ExchangeData data);
	void bidReceiptReceived(CommunicationsPublicIdentity participant, BarterMessageData.BidReceipt data);
	void biddingEnded(CommunicationsPublicIdentity participant, BarterMessageData.BiddingOver data);
	void concessionReceived(CommunicationsPublicIdentity participant, BarterMessageData.Concession data);
	void winClaimReceived(CommunicationsPublicIdentity participant, BidDivulgeData data);
	void barterOver(CommunicationsPublicIdentity participant, BarterMessageData.BarterEnd data);
}

