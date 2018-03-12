package net.roboticapex.selloff;

import net.roboticapex.selloff.messagedata.TradeMessageData;
import net.roboticapex.selloff.messagedata.BidCommitmentData;
import net.roboticapex.selloff.messagedata.TestData;
import net.roboticapex.selloff.messagedata.PromiseDivulgeData;
import net.roboticapex.senderReceivers.SenderReceiversPublicIdentity;

/**
 * Interface for users of the BidPal auction protocol.  
 * This is to communicate with the user (e.g. the bidder or seller), not for making the auction happen.
 * I.e., if you'd like to print a message to the screen or initiate some other action when a certain auction event occurs, 
 * this is the place to do it.  In some cases, it could easily be appropriate for these methods to do nothing at all. 
 */
public interface TradeUserAPI {
	void newTrade(SenderReceiversPublicIdentity user, TradeMessageData.TradeStart data);
	void promiseCommitmentReceived(SenderReceiversPublicIdentity user, BidCommitmentData data);
	void promiseTestingReceived(SenderReceiversPublicIdentity user, TestData data);
	void promiseReceiptReceived(SenderReceiversPublicIdentity user, TradeMessageData.PromiseReceipt data);
	void biddingEnded(SenderReceiversPublicIdentity user, TradeMessageData.BiddingOver data);
	void concessionReceived(SenderReceiversPublicIdentity user, TradeMessageData.Concession data);
	void winClaimReceived(SenderReceiversPublicIdentity user, PromiseDivulgeData data);
	void tradeOver(SenderReceiversPublicIdentity user, TradeMessageData.TradeEnd data);
}

