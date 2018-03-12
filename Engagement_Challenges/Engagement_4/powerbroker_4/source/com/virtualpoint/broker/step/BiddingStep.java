package com.virtualpoint.broker.step;

import com.virtualpoint.talkers.DialogsConnection;
import com.virtualpoint.talkers.DialogsTrouble;
import com.virtualpoint.broker.PurchasePlan;
import com.virtualpoint.broker.MyBarter;
import com.virtualpoint.broker.ProductIntermediaryBarter;
import com.virtualpoint.broker.ProductIntermediaryTrouble;
import com.virtualpoint.broker.Powerbrokermsg;
import com.virtualpoint.broker.barter.BarterAdapter;
import com.virtualpoint.logging.Logger;
import com.virtualpoint.logging.LoggerFactory;

import java.util.List;

public class BiddingStep extends BarterBaseStep {
    private final Logger logger = LoggerFactory.fetchLogger(getClass());

    private final PurchasePlan bidPlan;
    private final BarterAdapter barterAdapter;
    private int myBid = 0;

    /**
     *
     * @param barters the entire list of auctions that we'll be processing at some point.  For the purposes of this
     *                 phase (and most others) we're only every concerned with the first auction on this list. That's
     *                 the 'current' auction.
     * @param myBarters a list of auctions that I've started.
     * @param barterAdapter used to help us manage the current auction and the auction library.
     * @param stepOverseer
     */
    public BiddingStep(List<ProductIntermediaryBarter> barters, List<MyBarter> myBarters, BarterAdapter barterAdapter, StepOverseer stepOverseer) {
        super(barters, myBarters, stepOverseer);
        this.bidPlan = stepOverseer.takeBidPlan();
        this.barterAdapter = barterAdapter;
    }

    @Override
    public void enterStep() throws ProductIntermediaryTrouble {
        super.enterStep();

        // are we bidding on this auction? if so, bid
        ProductIntermediaryBarter currentBarter = takeCurrentBarter();

        try {
            if (isCurBarterMyBarter()) {
                boolean bidOnOurBarter = false;

                // find this in our auctions...
                for (int q = 0; q < myBarters.size(); q++) {
                    MyBarter myBarter = myBarters.get(q);

                    if (myBarter.pullId().equals(currentBarter.id)) {


                        logger.info("Going to make a reserve bid of " + myBarter.grabReserve());
                        myBid = myBarter.grabReserve();
                        bidOnOurBarter = true;
                    }
                    logger.info("have auction " + myBarter.pullId() + " reserve " + myBarter.grabReserve());
                }

                if (!bidOnOurBarter) {
                    enterStepWorker(currentBarter);
                }

            } else {
                // we always bid, even if it's 0
                enterStepEntity(currentBarter);
            }

            logger.info("Bidding " + myBid + " for " + currentBarter.productAmount + " units ");
            takeStepOverseer().takeProductIntermediaryUser().bidding(currentBarter.productAmount, myBid);
            barterAdapter.bid(currentBarter.id, myBid);
        } catch (Exception e) {
            throw new ProductIntermediaryTrouble(e);
        }
    }

    private void enterStepEntity(ProductIntermediaryBarter currentBarter) {
        myBid = bidPlan.calcAmountToBid(currentBarter);
    }

    private void enterStepWorker(ProductIntermediaryBarter currentBarter) throws ProductIntermediaryTrouble {
        throw new ProductIntermediaryTrouble("This is our auction, but we couldn't " +
                "find the MyAuction object associated with it " + currentBarter.id);
    }

    @Override
    public Step handleMsg(DialogsConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductIntermediaryTrouble {

        if (msg.getType() == Powerbrokermsg.BaseMessage.Type.AUCTION_DATA) {
            try {
                barterAdapter.handle(connection.pullTheirIdentity(), msg.getAuctionData().toByteArray());
            } catch (DialogsTrouble e) {
                throw new ProductIntermediaryTrouble(e);
            }

            if (!barterAdapter.hasReceivedAllExpectedBids()) {
                // we're still expecting more bids, we have to wait
                logger.info("handleMsg: still waiting for bids");
                return null;
            } else {

                // it may be time to start sending these finished messages
                if (isItMyTurnToTransferMessages()) {
                    handleMsgCoach();
                }
                return shouldTransitionToNextStep();
            }
        } else if (msg.getType() == Powerbrokermsg.BaseMessage.Type.BIDDING_FINISHED) {
            return handleMsgGuide(connection, msg);
        } else {
            return new BiddingStepTarget(msg).invoke();
        }
    }

    private Step handleMsgGuide(DialogsConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductIntermediaryTrouble {
        addFinalMessage(connection.pullTheirIdentity(), msg);

        if (isItMyTurnToTransferMessages()) {
            transferBiddingFinishedMessage();
        }

        return shouldTransitionToNextStep();
    }

    private void handleMsgCoach() throws ProductIntermediaryTrouble {
        transferBiddingFinishedMessage();
    }

    @Override
    protected Step nextStep() throws ProductIntermediaryTrouble {
        return new BarterEndStep(barters, myBarters, bidPlan, barterAdapter, myBid, takeStepOverseer());
    }

    private void transferBiddingFinishedMessage() throws ProductIntermediaryTrouble {
        Powerbrokermsg.BaseMessage baseMessage = Powerbrokermsg.BaseMessage.newBuilder()
                .setType(Powerbrokermsg.BaseMessage.Type.BIDDING_FINISHED)
                .build();

        transferFinalMessage(baseMessage.toByteArray());
    }

    private class BiddingStepTarget {
        private Powerbrokermsg.BaseMessage msg;

        public BiddingStepTarget(Powerbrokermsg.BaseMessage msg) {
            this.msg = msg;
        }

        public Step invoke() {
            logger.error("Invalid message type in BiddingPhase: " + msg.getType());
            return null;
        }
    }
}
