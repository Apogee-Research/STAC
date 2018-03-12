package org.digitalapex.trade;

import org.digitalapex.trade.messagedata.SelloffMessageData;
import org.digitalapex.trade.messagedata.SelloffMessageData.SelloffEnd;
import org.digitalapex.trade.messagedata.SelloffMessageData.SelloffStart;
import org.digitalapex.trade.messagedata.SelloffMessageData.BidReceipt;
import org.digitalapex.trade.messagedata.SelloffMessageData.BiddingOver;
import org.digitalapex.trade.messagedata.SelloffMessageData.Concession;
import org.digitalapex.trade.messagedata.SelloffSerializer;
import org.digitalapex.trade.messagedata.PromiseData;
import org.digitalapex.trade.messagedata.OfferAnalysisData;
import org.digitalapex.trade.messagedata.BidConveyData;
import org.digitalapex.talkers.TalkersRaiser;
import org.digitalapex.talkers.TalkersIdentity;
import org.digitalapex.talkers.TalkersPublicIdentity;
import org.digitalapex.talkers.Communicator;


/**
 * CommsHandler (and Communicator) for secret bidding.
 *
 */
public class SelloffCoach {

    private SelloffOperator operator;

    private final TalkersIdentity identity; // identity of this user

    private SelloffSerializer serializer;

    private SelloffUserAPI userAPI;

    public SelloffCoach(SelloffOperator operator, Communicator communicator, SelloffUserAPI userAPI, SelloffSerializer serializer, int port, TalkersIdentity identity, int maxBid) {
        this.serializer = serializer;
        this.identity = identity;
        this.userAPI = userAPI;
        this.operator = operator;
    }


    public synchronized void handle(TalkersPublicIdentity user, byte[] msgData) throws TalkersRaiser {
        try {
            SelloffMessageData data = serializer.deserialize(msgData);
            String selloffId = data.fetchSelloffId();
            switch (data.type) {
                case BID_COMMITMENT:
                    userAPI.bidCovenantReceived(user, (PromiseData) data);
                    operator.processPledge(user, (PromiseData) data);
                    break;
                case BID_COMPARISON:
                    userAPI.bidObservationReceived(user, (OfferAnalysisData) data);
                    operator.processObservation(user, (OfferAnalysisData) data);
                    break;
                case BID_RECEIPT:
                    userAPI.bidReceiptReceived(user, (BidReceipt)data);
                    // no action required
                    break;
                case AUCTION_START:
                    SelloffStart startData = (SelloffStart) data;
                    userAPI.newSelloff(user, startData);
                    operator.processNewSelloff(user, selloffId, startData.description);
                    break;
                case BIDDING_OVER:
                    userAPI.biddingEnded(user, (BiddingOver) data);
                    operator.biddingOver(user, selloffId);
                    break;
                case AUCTION_END:
                    SelloffEnd endData = (SelloffEnd) data;
                    userAPI.selloffOver(user, endData);
                    operator.processSelloffEnd(user, selloffId, endData.winner, endData.winningBid);
                    break;
                case CLAIM_WIN:
                    BidConveyData conveyData = (BidConveyData) data;
                    userAPI.winClaimReceived(user, conveyData);
                    operator.processWinClaim(user, selloffId, conveyData);
                    break;
                case CONCESSION:
                    userAPI.concessionReceived(user, (Concession) data);
                    operator.processConcession(user, selloffId);
                    break;
                default:
                    System.err.println(identity.pullId() + " received an unknown message " + data.type);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void addUser(TalkersPublicIdentity user) {
        operator.addUser(user);
    }

    public void removeUser(TalkersPublicIdentity user) {
        operator.removeUser(user);
    }
}