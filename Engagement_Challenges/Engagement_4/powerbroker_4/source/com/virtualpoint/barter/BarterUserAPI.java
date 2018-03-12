package com.virtualpoint.barter;

import com.virtualpoint.barter.messagedata.BarterMessageData;
import com.virtualpoint.barter.messagedata.OfferSubmission;
import com.virtualpoint.barter.messagedata.ExchangeData;
import com.virtualpoint.barter.messagedata.BidConveyData;
import com.virtualpoint.talkers.DialogsPublicIdentity;

/**
 * Interface for users of the BidPal auction protocol.  
 * This is to communicate with the user (e.g. the bidder or seller), not for making the auction happen.
 * I.e., if you'd like to print a message to the screen or initiate some other action when a certain auction event occurs, 
 * this is the place to do it.  In some cases, it could easily be appropriate for these methods to do nothing at all. 
 */
public interface BarterUserAPI {
	void newBarter(DialogsPublicIdentity user, BarterMessageData.BarterStart data);
	void bidContractReceived(DialogsPublicIdentity user, OfferSubmission data);
	void bidTestingReceived(DialogsPublicIdentity user, ExchangeData data);
	void bidReceiptReceived(DialogsPublicIdentity user, BarterMessageData.BidReceipt data);
	void biddingEnded(DialogsPublicIdentity user, BarterMessageData.BiddingOver data);
	void concessionReceived(DialogsPublicIdentity user, BarterMessageData.Concession data);
	void winClaimReceived(DialogsPublicIdentity user, BidConveyData data);
	void barterOver(DialogsPublicIdentity user, BarterMessageData.BarterEnd data);
}

