package net.roboticapex.selloff;

import net.roboticapex.selloff.messagedata.TradeMessageData;
import net.roboticapex.selloff.messagedata.TradeMessageData.TradeEnd;
import net.roboticapex.selloff.messagedata.TradeMessageData.TradeStart;
import net.roboticapex.selloff.messagedata.TradeMessageData.PromiseReceipt;
import net.roboticapex.selloff.messagedata.TradeMessageData.BiddingOver;
import net.roboticapex.selloff.messagedata.TradeMessageData.Concession;
import net.roboticapex.selloff.messagedata.TradeSerializer;
import net.roboticapex.selloff.messagedata.BidCommitmentData;
import net.roboticapex.selloff.messagedata.TestData;
import net.roboticapex.selloff.messagedata.PromiseDivulgeData;
import net.roboticapex.senderReceivers.SenderReceiversDeviation;
import net.roboticapex.senderReceivers.SenderReceiversIdentity;
import net.roboticapex.senderReceivers.SenderReceiversPublicIdentity;
import net.roboticapex.senderReceivers.Communicator;


/**
 * CommsHandler (and Communicator) for secret bidding.
 *
 */
public class TradeHandler {

    private TradeOperator operator;

    private final SenderReceiversIdentity identity; // identity of this user

    private TradeSerializer serializer;

    private TradeUserAPI userAPI;

    public TradeHandler(TradeOperator operator, Communicator communicator, TradeUserAPI userAPI, TradeSerializer serializer, int port, SenderReceiversIdentity identity, int maxPromise) {
        this.serializer = serializer;
        this.identity = identity;
        this.userAPI = userAPI;
        this.operator = operator;
    }


    public synchronized void handle(SenderReceiversPublicIdentity user, byte[] msgData) throws SenderReceiversDeviation {
        try {
            TradeMessageData data = serializer.deserialize(msgData);
            String tradeId = data.obtainTradeId();
            switch (data.type) {
                case BID_COMMITMENT:
                    userAPI.promiseCommitmentReceived(user, (BidCommitmentData) data);
                    operator.processPledge(user, (BidCommitmentData) data);
                    break;
                case BID_COMPARISON:
                    userAPI.promiseTestingReceived(user, (TestData) data);
                    operator.processTesting(user, (TestData) data);
                    break;
                case BID_RECEIPT:
                    userAPI.promiseReceiptReceived(user, (PromiseReceipt) data);
                    // no action required
                    break;
                case AUCTION_START:
                    TradeStart startData = (TradeStart) data;
                    userAPI.newTrade(user, startData);
                    operator.processNewTrade(user, tradeId, startData.description);
                    break;
                case BIDDING_OVER:
                    userAPI.biddingEnded(user, (BiddingOver) data);
                    operator.biddingOver(user, tradeId);
                    break;
                case AUCTION_END:
                    TradeEnd endData = (TradeEnd) data;
                    userAPI.tradeOver(user, endData);
                    operator.processTradeEnd(user, tradeId, endData.winner, endData.winningPromise);
                    break;
                case CLAIM_WIN:
                    PromiseDivulgeData divulgeData = (PromiseDivulgeData) data;
                    userAPI.winClaimReceived(user, divulgeData);
                    operator.processWinClaim(user, tradeId, divulgeData);
                    break;
                case CONCESSION:
                    userAPI.concessionReceived(user, (Concession) data);
                    operator.processConcession(user, tradeId);
                    break;
                default:
                    System.err.println(identity.obtainId() + " received an unknown message " + data.type);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void addUser(SenderReceiversPublicIdentity user) {
        operator.addUser(user);
    }

    public void removeUser(SenderReceiversPublicIdentity user) {
        operator.removeUser(user);
    }
}