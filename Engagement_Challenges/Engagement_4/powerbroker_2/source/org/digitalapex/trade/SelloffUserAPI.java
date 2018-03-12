package org.digitalapex.trade;

import org.digitalapex.trade.messagedata.SelloffMessageData;
import org.digitalapex.trade.messagedata.PromiseData;
import org.digitalapex.trade.messagedata.OfferAnalysisData;
import org.digitalapex.trade.messagedata.BidConveyData;
import org.digitalapex.talkers.TalkersPublicIdentity;

/**
 * Interface for users of the BidPal auction protocol.  
 * This is to communicate with the user (e.g. the bidder or seller), not for making the auction happen.
 * I.e., if you'd like to print a message to the screen or initiate some other action when a certain auction event occurs, 
 * this is the place to do it.  In some cases, it could easily be appropriate for these methods to do nothing at all. 
 */
public interface SelloffUserAPI {
	void newSelloff(TalkersPublicIdentity user, SelloffMessageData.SelloffStart data);
	void bidCovenantReceived(TalkersPublicIdentity user, PromiseData data);
	void bidObservationReceived(TalkersPublicIdentity user, OfferAnalysisData data);
	void bidReceiptReceived(TalkersPublicIdentity user, SelloffMessageData.BidReceipt data);
	void biddingEnded(TalkersPublicIdentity user, SelloffMessageData.BiddingOver data);
	void concessionReceived(TalkersPublicIdentity user, SelloffMessageData.Concession data);
	void winClaimReceived(TalkersPublicIdentity user, BidConveyData data);
	void selloffOver(TalkersPublicIdentity user, SelloffMessageData.SelloffEnd data);
}

