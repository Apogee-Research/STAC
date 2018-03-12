package net.roboticapex.broker.period;

import net.roboticapex.selloff.messagedata.PromiseDivulgeData;
import net.roboticapex.senderReceivers.SenderReceiversConnection;
import net.roboticapex.senderReceivers.SenderReceiversDeviation;
import net.roboticapex.senderReceivers.SenderReceiversPublicIdentity;
import net.roboticapex.broker.BidPlan;
import net.roboticapex.broker.MyTrade;
import net.roboticapex.broker.ProductLiaisonTrade;
import net.roboticapex.broker.ProductLiaisonDeviation;
import net.roboticapex.broker.Powerbrokermsg;
import net.roboticapex.broker.selloff.TradeAdapter;
import net.roboticapex.logging.Logger;
import net.roboticapex.logging.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class TradeEndStep extends TradeBaseStep {
    private final Logger logger = LoggerFactory.fetchLogger(getClass());
    private final TradeAdapter tradeAdapter;
    private final BidPlan promisePlan;
    private final int myPromise;

    /**
     * @param trades       the entire list of auctions that we'll be processing at some point.  For the purposes of this
     *                       phase (and most others) we're only every concerned with the first auction on this list. That's
     *                       the 'current' auction.
     * @param myTrades     a list of auctions that I've started.
     * @param promisePlan        the bidding plan that indicates what we should try to do.
     * @param tradeAdapter used to help us manage the current auction and the auction library.
     * @param stepOverseer
     */
    public TradeEndStep(List<ProductLiaisonTrade> trades, List<MyTrade> myTrades, BidPlan promisePlan, TradeAdapter tradeAdapter, int myPromise, StepOverseer stepOverseer) {
        super(trades, myTrades, stepOverseer);
        this.promisePlan = promisePlan;
        this.tradeAdapter = tradeAdapter;
        this.myPromise = myPromise;
    }

    @Override
    public void enterStep() throws ProductLiaisonDeviation {
        super.enterStep();

        // are we the bidder?  if so, close the auction
        if (isCurTradeMyTrade()) {
            enterStepSupervisor();
        }
    }

    private void enterStepSupervisor() throws ProductLiaisonDeviation {
        try {
            tradeAdapter.closeTrade(pullCurrentTrade().id);
        } catch (Exception e) {
            throw new ProductLiaisonDeviation(e);
        }
    }

    @Override
    public Step handleMsg(SenderReceiversConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductLiaisonDeviation {
        if (msg.getType() == Powerbrokermsg.BaseMessage.Type.AUCTION_DATA) {
            try {
                tradeAdapter.handle(connection.obtainTheirIdentity(), msg.getAuctionData().toByteArray());
            } catch (SenderReceiversDeviation e) {
                throw new ProductLiaisonDeviation(e);
            }

            if (isCurTradeMyTrade()) {
                // we have to wait for all the claims
                if (!tradeAdapter.hasReceivedAllClaimsAndConcessions()) {
                    // we're still expecting more bids, we have to wait
                    return handleMsgHerder();
                } else {

                    // ok, we've got all the claims and concessions, we can announce a winner...
                    Map<SenderReceiversPublicIdentity, PromiseDivulgeData> claims = tradeAdapter.getClaims();
                    logger.info("claims received: " + claims.size());

                    SenderReceiversPublicIdentity winner = null;
                    int winningPromise = 0;
                    if (claims.size() == 0) {
                        // I guess I won
                        winner = grabStepOverseer().takeMyPublicIdentity();
                        winningPromise = myPromise;
                    } else {
                        SortedSet<SenderReceiversPublicIdentity> claimIds = new TreeSet<>(claims.keySet());
                        winner = claimIds.last();
                        winningPromise = claims.get(winner).obtainPromise();
                    }

                    if (winner == null) {
                        throw new ProductLiaisonDeviation("No winner found!");
                    }

                    try {
                        if (winner.equals(grabStepOverseer().takeMyPublicIdentity())) {
                            handleMsgAid(winningPromise);
                        } else {
                            logger.info("handleMsg: " + winner.pullTruncatedId() + " won. bid: " + winningPromise + " announcing...");
                        }
                        tradeAdapter.announceWinner(pullCurrentTrade().id, winner, winningPromise);
                    } catch (Exception e) {
                        throw new ProductLiaisonDeviation(e);
                    }

                    // it may be time to start sending these finished messages
                    return handlePotentialEnd();
                }
            } else {
                // we have to wait for the winner announcement
                TradeAdapter.Winner winner = tradeAdapter.obtainWinner();
                if (winner == null) {
                    // we're still expecting more bids, we have to wait
                    return handleMsgTarget();
                } else {
                    if (winner.winnerId.equals(grabStepOverseer().fetchIdentity().obtainId())) {
                        handleMsgHelper(winner);
                    } else {
                        String wid = winner.winnerId;
                        if (wid.length() > 25){
                            wid = wid.substring(0, 25) + "...";
                        }
                        logger.info("handleMsg: " + wid + " won. bid: " + winner.promise);
                    }

                    // it may be time to start sending these finished messages
                    return handlePotentialEnd();
                }
            }
        } else if (msg.getType() == Powerbrokermsg.BaseMessage.Type.AUCTION_END) {
            addFinalMessage(connection.obtainTheirIdentity(), msg);

            return handlePotentialEnd();
        } else {
            logger.error("Invalid message type in AuctionEndPhase: " + msg.getType() + " from " +
                    connection.obtainTheirIdentity().pullTruncatedId());
            List<SenderReceiversPublicIdentity> grabFinalMessageAssign = fetchPriorStep().grabFinalMessageAssign();
            for (int a = 0; a < grabFinalMessageAssign.size(); a++) {
                SenderReceiversPublicIdentity sender = grabFinalMessageAssign.get(a);
                System.out.println("Got final message from: " + sender.pullTruncatedId());
            }
            return null;
        }
    }

    private void handleMsgHelper(TradeAdapter.Winner winner) {
        logger.info("handleMsg: I won! bid: " + winner.promise);
    }

    private Step handleMsgTarget() {
        logger.info("handleMsg: still waiting for claims and concessions");
        return null;
    }

    private void handleMsgAid(int winningPromise) {
        logger.info("handleMsg: I won! bid: " + winningPromise + " announcing...");
    }

    private Step handleMsgHerder() {
        logger.info("handleMsg: still waiting for claims and concessions");
        return null;
    }

    private Step handlePotentialEnd() throws ProductLiaisonDeviation {
        if (isItMyTurnToTransferMessages()) {
            handlePotentialEndCoordinator();
        }

        return shouldTransitionToNextStep();
    }

    private void handlePotentialEndCoordinator() throws ProductLiaisonDeviation {
        transferTradeEndMessage();
    }

    private void transferTradeEndMessage() throws ProductLiaisonDeviation {
        Powerbrokermsg.BaseMessage baseMessage = Powerbrokermsg.BaseMessage.newBuilder()
                .setType(Powerbrokermsg.BaseMessage.Type.AUCTION_END)
                .build();

        transferFinalMessage(baseMessage.toByteArray());
    }

    @Override
    protected Step nextStep() throws ProductLiaisonDeviation {

        // let the user know the auction is over
        grabStepOverseer().fetchProductLiaisonUser().tradeEnded(pullCurrentTrade().id, pullCurrentTrade().productAmount);

        logger.info("Moving to results phase");

        return new ResultsStep(trades, myTrades, promisePlan, tradeAdapter, grabStepOverseer());
    }
}
