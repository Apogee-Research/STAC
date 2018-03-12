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

public class BarterBeginStep extends BarterBaseStep {

    private final Logger logger = LoggerFactory.fetchLogger(getClass());
    private final PurchasePlan bidPlan;
    private final BarterAdapter barterAdapter;

    /**
     *
     * @param barters the entire list of auctions that we'll be processing at some point.  For the purposes of this
     *                 phase (and most others) we're only ever concerned with the first auction on this list. That's
     *                 the 'current' auction.
     * @param myBarters a list of auctions that I've started.
     * @param stepOverseer
     */
    public BarterBeginStep(List<ProductIntermediaryBarter> barters, List<MyBarter> myBarters, StepOverseer stepOverseer) {
        super(barters, myBarters, stepOverseer);
        this.bidPlan = stepOverseer.takeBidPlan();
        this.barterAdapter = new BarterAdapter(stepOverseer.getIdentity(), stepOverseer.takeConnections());
    }

    @Override
    public void enterStep() throws ProductIntermediaryTrouble {
        super.enterStep();

        // is this my auction or someone else's?
        // (we're only talking about the current auction)
        if (isItMyTurnToTransferMessages()) {
            enterStepHelper();
        } else {
            enterStepWorker();
        }
    }

    private void enterStepWorker() {
        new BarterBeginStepAdviser().invoke();
    }

    private void enterStepHelper() throws ProductIntermediaryTrouble {
        if (isCurBarterMyBarter()) {
            new BarterBeginStepService().invoke();
        } else {
            enterStepHelperAssist();
        }
    }

    private void enterStepHelperAssist() throws ProductIntermediaryTrouble {
        transferBarterBeginMsg();
    }

    @Override
    public Step handleMsg(DialogsConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductIntermediaryTrouble {

        if (msg.getType() == Powerbrokermsg.BaseMessage.Type.AUCTION_DATA) {
            try {
                barterAdapter.handle(connection.pullTheirIdentity(), msg.getAuctionData().toByteArray());
            } catch (DialogsTrouble e) {
                throw new ProductIntermediaryTrouble(e);
            }

            // we don't need to do any further processing...
            return null;
        }

        if (msg.getType() != Powerbrokermsg.BaseMessage.Type.AUCTION_BEGIN) {
            return new BarterBeginStepHelp(msg).invoke();
        }

        logger.info("received begin message from " + connection.pullTheirIdentity().takeTruncatedId());

        addFinalMessage(connection.pullTheirIdentity(), msg);

        String expectedId = takeCurrentBarter().id;
        if (!msg.getAuctionBegin().getAuctionId().equals(expectedId)) {
            throw new ProductIntermediaryTrouble("Unexpected auction id. Expected: [" + expectedId + "] got ["
                    + msg.getAuctionBegin().getAuctionId());
        }

        if (!hasSentFinalMessage) {
            if (isItMyTurnToTransferMessages()) {
                if (isCurBarterMyBarter()) {
                    startBarter();
                } else {
                    handleMsgAid();
                }
            }
        }

        return shouldTransitionToNextStep();
    }

    private void handleMsgAid() throws ProductIntermediaryTrouble {
        logger.info("We're observing this auction, we might bid");
        // send auction begin message and move on
        transferBarterBeginMsg();
    }

    private void startBarter() throws ProductIntermediaryTrouble {

        logger.info("Starting auction...");

        ProductIntermediaryBarter curBarter = takeCurrentBarter();
        MyBarter myBarter = null;

        // find our matching auction
        // makes the assumption that *we* either won't have two auctions
        // for the same amount, or, if we do, that it doesn't
        // matter what order they're resolved in.
        for (int p = 0; p < myBarters.size(); p++) {
            MyBarter curMyBarter = myBarters.get(p);
            if (curMyBarter.grabAmountOfProduct() == curBarter.productAmount) {
                myBarter = curMyBarter;
                break;
            }
        }

        if (myBarter == null) {
            throw new ProductIntermediaryTrouble("This is our auction but we have no record of it. PowerAmount: " +
                    curBarter.productAmount);
        }

        // we're selling!
        logger.info("We're selling " + curBarter.productAmount + " units of power for at least " +
                myBarter.grabReserve());

        try {
            barterAdapter.startBarter(myBarter.pullId(), myBarter.toString());
        } catch (DialogsTrouble e) {
            throw new ProductIntermediaryTrouble(e);
        }

        transferBarterBeginMsg();
    }

    private void transferBarterBeginMsg() throws ProductIntermediaryTrouble {

        logger.info("Sending auction begin message...");

        // let the user know
        takeStepOverseer().takeProductIntermediaryUser().barterStarted(takeCurrentBarter().id, takeCurrentBarter().productAmount);

        ProductIntermediaryBarter curBarter = takeCurrentBarter();

        // We're going to send a message indicating we're going to join in the bidding on this auction.
        // Then we're going to wait for everyone to confirm this is the current auction.
        // (Which they'll do by just repeating the message back to everyone)
        Powerbrokermsg.AuctionBeginMessage beginMessage = Powerbrokermsg.AuctionBeginMessage.newBuilder()
                .setPowerAmount(curBarter.productAmount)
                .setAuctionId(curBarter.id)
                .build();

        Powerbrokermsg.BaseMessage baseMessage = Powerbrokermsg.BaseMessage.newBuilder()
                .setAuctionBegin(beginMessage)
                .setType(Powerbrokermsg.BaseMessage.Type.AUCTION_BEGIN)
                .build();

        transferFinalMessage(baseMessage.toByteArray());
    }

    protected Step nextStep() throws ProductIntermediaryTrouble {
        logger.info("Moving to bidding phase");
        return new BiddingStep(barters, myBarters, barterAdapter, takeStepOverseer());
    }

    private class BarterBeginStepService {
        public void invoke() throws ProductIntermediaryTrouble {
            startBarter();
        }
    }

    private class BarterBeginStepAdviser {
        public void invoke() {
            logger.info("Not my turn to send message");
        }
    }

    private class BarterBeginStepHelp {
        private Powerbrokermsg.BaseMessage msg;

        public BarterBeginStepHelp(Powerbrokermsg.BaseMessage msg) {
            this.msg = msg;
        }

        public Step invoke() {
            logger.error("Invalid message type in AuctionBeginPhase: " + msg.getType());
            return null;
        }
    }
}
