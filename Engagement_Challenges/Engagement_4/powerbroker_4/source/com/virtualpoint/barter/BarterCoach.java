package com.virtualpoint.barter;

import com.virtualpoint.barter.messagedata.BarterMessageData;
import com.virtualpoint.barter.messagedata.BarterMessageData.BarterEnd;
import com.virtualpoint.barter.messagedata.BarterMessageData.BarterStart;
import com.virtualpoint.barter.messagedata.BarterMessageData.BidReceipt;
import com.virtualpoint.barter.messagedata.BarterMessageData.BiddingOver;
import com.virtualpoint.barter.messagedata.BarterMessageData.Concession;
import com.virtualpoint.barter.messagedata.BarterSerializer;
import com.virtualpoint.barter.messagedata.OfferSubmission;
import com.virtualpoint.barter.messagedata.ExchangeData;
import com.virtualpoint.barter.messagedata.BidConveyData;
import com.virtualpoint.talkers.DialogsTrouble;
import com.virtualpoint.talkers.DialogsIdentity;
import com.virtualpoint.talkers.DialogsPublicIdentity;
import com.virtualpoint.talkers.Communicator;


/**
 * CommsHandler (and Communicator) for secret bidding.
 *
 */
public class BarterCoach {

    private BarterOperator operator;

    private final DialogsIdentity identity; // identity of this user

    private BarterSerializer serializer;

    private BarterUserAPI userAPI;

    public BarterCoach(BarterOperator operator, Communicator communicator, BarterUserAPI userAPI, BarterSerializer serializer, int port, DialogsIdentity identity, int maxBid) {
        this.serializer = serializer;
        this.identity = identity;
        this.userAPI = userAPI;
        this.operator = operator;
    }


    public synchronized void handle(DialogsPublicIdentity user, byte[] msgData) throws DialogsTrouble {
        try {
            BarterMessageData data = serializer.deserialize(msgData);
            String barterId = data.obtainBarterId();
            switch (data.type) {
                case BID_COMMITMENT:
                    userAPI.bidContractReceived(user, (OfferSubmission) data);
                    operator.processPledge(user, (OfferSubmission) data);
                    break;
                case BID_COMPARISON:
                    userAPI.bidTestingReceived(user, (ExchangeData) data);
                    operator.processTesting(user, (ExchangeData) data);
                    break;
                case BID_RECEIPT:
                    userAPI.bidReceiptReceived(user, (BidReceipt)data);
                    // no action required
                    break;
                case AUCTION_START:
                    BarterStart startData = (BarterStart) data;
                    userAPI.newBarter(user, startData);
                    operator.processNewBarter(user, barterId, startData.description);
                    break;
                case BIDDING_OVER:
                    userAPI.biddingEnded(user, (BiddingOver) data);
                    operator.biddingOver(user, barterId);
                    break;
                case AUCTION_END:
                    BarterEnd endData = (BarterEnd) data;
                    userAPI.barterOver(user, endData);
                    operator.processBarterEnd(user, barterId, endData.winner, endData.winningBid);
                    break;
                case CLAIM_WIN:
                    BidConveyData conveyData = (BidConveyData) data;
                    userAPI.winClaimReceived(user, conveyData);
                    operator.processWinClaim(user, barterId, conveyData);
                    break;
                case CONCESSION:
                    userAPI.concessionReceived(user, (Concession) data);
                    operator.processConcession(user, barterId);
                    break;
                default:
                    System.err.println(identity.grabId() + " received an unknown message " + data.type);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void addUser(DialogsPublicIdentity user) {
        operator.addUser(user);
    }

    public void removeUser(DialogsPublicIdentity user) {
        operator.removeUser(user);
    }
}