package com.virtualpoint.broker.step;

import com.virtualpoint.barter.messagedata.BidConveyData;
import com.virtualpoint.talkers.DialogsConnection;
import com.virtualpoint.talkers.DialogsTrouble;
import com.virtualpoint.talkers.DialogsPublicIdentity;
import com.virtualpoint.broker.PurchasePlan;
import com.virtualpoint.broker.MyBarter;
import com.virtualpoint.broker.ProductIntermediaryBarter;
import com.virtualpoint.broker.ProductIntermediaryTrouble;
import com.virtualpoint.broker.Powerbrokermsg;
import com.virtualpoint.broker.barter.BarterAdapter;
import com.virtualpoint.logging.Logger;
import com.virtualpoint.logging.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class BarterEndStep extends BarterBaseStep {
    private final Logger logger = LoggerFactory.fetchLogger(getClass());
    private final BarterAdapter barterAdapter;
    private final PurchasePlan bidPlan;
    private final int myBid;

    /**
     * @param barters       the entire list of auctions that we'll be processing at some point.  For the purposes of this
     *                       phase (and most others) we're only every concerned with the first auction on this list. That's
     *                       the 'current' auction.
     * @param myBarters     a list of auctions that I've started.
     * @param bidPlan        the bidding plan that indicates what we should try to do.
     * @param barterAdapter used to help us manage the current auction and the auction library.
     * @param stepOverseer
     */
    public BarterEndStep(List<ProductIntermediaryBarter> barters, List<MyBarter> myBarters, PurchasePlan bidPlan, BarterAdapter barterAdapter, int myBid, StepOverseer stepOverseer) {
        super(barters, myBarters, stepOverseer);
        this.bidPlan = bidPlan;
        this.barterAdapter = barterAdapter;
        this.myBid = myBid;
    }

    @Override
    public void enterStep() throws ProductIntermediaryTrouble {
        super.enterStep();

        // are we the bidder?  if so, close the auction
        if (isCurBarterMyBarter()) {
            try {
                barterAdapter.closeBarter(takeCurrentBarter().id);
            } catch (Exception e) {
                throw new ProductIntermediaryTrouble(e);
            }
        }
    }

    @Override
    public Step handleMsg(DialogsConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductIntermediaryTrouble {
        if (msg.getType() == Powerbrokermsg.BaseMessage.Type.AUCTION_DATA) {
            try {
                barterAdapter.handle(connection.pullTheirIdentity(), msg.getAuctionData().toByteArray());
            } catch (DialogsTrouble e) {
                throw new ProductIntermediaryTrouble(e);
            }

            if (isCurBarterMyBarter()) {
                // we have to wait for all the claims
                if (!barterAdapter.hasReceivedAllClaimsAndConcessions()) {
                    // we're still expecting more bids, we have to wait
                    return handleMsgEngine();
                } else {

                    // ok, we've got all the claims and concessions, we can announce a winner...
                    Map<DialogsPublicIdentity, BidConveyData> claims = barterAdapter.getClaims();
                    logger.info("claims received: " + claims.size());

                    DialogsPublicIdentity winner = null;
                    int winningBid = 0;
                    if (claims.size() == 0) {
                        // I guess I won
                        winner = takeStepOverseer().getMyPublicIdentity();
                        winningBid = myBid;
                    } else {
                        SortedSet<DialogsPublicIdentity> claimIds = new TreeSet<>(claims.keySet());
                        winner = claimIds.last();
                        winningBid = claims.get(winner).takeBid();
                    }

                    if (winner == null) {
                        return handleMsgAdviser();
                    }

                    try {
                        if (winner.equals(takeStepOverseer().getMyPublicIdentity())) {
                            handleMsgCoordinator(winningBid);
                        } else {
                            handleMsgHome(winner, winningBid);
                        }
                        barterAdapter.announceWinner(takeCurrentBarter().id, winner, winningBid);
                    } catch (Exception e) {
                        throw new ProductIntermediaryTrouble(e);
                    }

                    // it may be time to start sending these finished messages
                    return handlePotentialEnd();
                }
            } else {
                // we have to wait for the winner announcement
                BarterAdapter.Winner winner = barterAdapter.takeWinner();
                if (winner == null) {
                    // we're still expecting more bids, we have to wait
                    logger.info("handleMsg: still waiting for claims and concessions");
                    return null;
                } else {
                    if (winner.winnerId.equals(takeStepOverseer().getIdentity().grabId())) {
                        logger.info("handleMsg: I won! bid: " + winner.bid);
                    } else {
                        String wid = winner.winnerId;
                        if (wid.length() > 25){
                            wid = wid.substring(0, 25) + "...";
                        }
                        logger.info("handleMsg: " + wid + " won. bid: " + winner.bid);
                    }

                    // it may be time to start sending these finished messages
                    return handlePotentialEnd();
                }
            }
        } else if (msg.getType() == Powerbrokermsg.BaseMessage.Type.AUCTION_END) {
            addFinalMessage(connection.pullTheirIdentity(), msg);

            return handlePotentialEnd();
        } else {
            logger.error("Invalid message type in AuctionEndPhase: " + msg.getType() + " from " +
                    connection.pullTheirIdentity().takeTruncatedId());
            List<DialogsPublicIdentity> pullFinalMessageAssign = pullPriorStep().pullFinalMessageAssign();
            for (int q = 0; q < pullFinalMessageAssign.size(); ) {
                for (; (q < pullFinalMessageAssign.size()) && (Math.random() < 0.6); q++) {
                    DialogsPublicIdentity sender = pullFinalMessageAssign.get(q);
                    System.out.println("Got final message from: " + sender.takeTruncatedId());
                }
            }
            return null;
        }
    }

    private void handleMsgHome(DialogsPublicIdentity winner, int winningBid) {
        logger.info("handleMsg: " + winner.takeTruncatedId() + " won. bid: " + winningBid + " announcing...");
    }

    private void handleMsgCoordinator(int winningBid) {
        logger.info("handleMsg: I won! bid: " + winningBid + " announcing...");
    }

    private Step handleMsgAdviser() throws ProductIntermediaryTrouble {
        throw new ProductIntermediaryTrouble("No winner found!");
    }

    private Step handleMsgEngine() {
        logger.info("handleMsg: still waiting for claims and concessions");
        return null;
    }

    private Step handlePotentialEnd() throws ProductIntermediaryTrouble {
        if (isItMyTurnToTransferMessages()) {
            handlePotentialEndAssist();
        }

        return shouldTransitionToNextStep();
    }

    private void handlePotentialEndAssist() throws ProductIntermediaryTrouble {
        transferBarterEndMessage();
    }

    private void transferBarterEndMessage() throws ProductIntermediaryTrouble {
        Powerbrokermsg.BaseMessage baseMessage = Powerbrokermsg.BaseMessage.newBuilder()
                .setType(Powerbrokermsg.BaseMessage.Type.AUCTION_END)
                .build();

        transferFinalMessage(baseMessage.toByteArray());
    }

    @Override
    protected Step nextStep() throws ProductIntermediaryTrouble {

        // let the user know the auction is over
        takeStepOverseer().takeProductIntermediaryUser().barterEnded(takeCurrentBarter().id, takeCurrentBarter().productAmount);

        logger.info("Moving to results phase");

        return new ResultsStep(barters, myBarters, bidPlan, barterAdapter, takeStepOverseer());
    }
}
