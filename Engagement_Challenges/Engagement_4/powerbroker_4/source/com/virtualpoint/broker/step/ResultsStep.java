package com.virtualpoint.broker.step;

import com.virtualpoint.talkers.DialogsConnection;
import com.virtualpoint.broker.PurchasePlan;
import com.virtualpoint.broker.MyBarter;
import com.virtualpoint.broker.ProductIntermediaryBarter;
import com.virtualpoint.broker.ProductIntermediaryTrouble;
import com.virtualpoint.broker.Powerbrokermsg;
import com.virtualpoint.broker.barter.BarterAdapter;
import com.virtualpoint.logging.Logger;
import com.virtualpoint.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ResultsStep extends BarterBaseStep {

    private final Logger logger = LoggerFactory.fetchLogger(getClass());

    private final PurchasePlan bidPlan;
    private final BarterAdapter barterAdapter;

    /**
     *
     * @param barters the entire list of auctions that we'll be processing at some point.  For the purposes of this
     *                 phase (and most others) we're only every concerned with the first auction on this list. That's
     *                 the 'current' auction.
     * @param myBarters a list of auctions that I've started.
     * @param bidPlan the bidding plan that indicates what we should try to do.
     * @param barterAdapter used to help us manage the current auction and the auction library.
     * @param stepOverseer
     */
    public ResultsStep(List<ProductIntermediaryBarter> barters, List<MyBarter> myBarters, PurchasePlan bidPlan, BarterAdapter barterAdapter, StepOverseer stepOverseer) {
        super(barters, myBarters, stepOverseer);
        this.bidPlan = bidPlan;
        this.barterAdapter = barterAdapter;
    }

    @Override
    public void enterStep() throws ProductIntermediaryTrouble {
        super.enterStep();

        BarterAdapter.Winner winner = barterAdapter.takeWinner();

        if (winner.winnerId.equals(takeStepOverseer().getIdentity().grabId())) {
            logger.info("I won. bid: " + winner.bid);
        } else {
            String id = winner.winnerId;
            if (id.length() > 25){
                id = id.substring(0, 25) + "...";
            }
            logger.info(id + " won. bid: " + winner.bid);
        }

        if (isItMyTurnToTransferMessages()) {
            new ResultsStepHerder().invoke();
        }
    }

    @Override
    public Step handleMsg(DialogsConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductIntermediaryTrouble {
        if (msg.getType() == Powerbrokermsg.BaseMessage.Type.RESULTS_END) {

            logger.info("recevied RESULTS_END from " + connection.pullTheirIdentity().takeTruncatedId());

            addFinalMessage(connection.pullTheirIdentity(), msg);

            if (isItMyTurnToTransferMessages()) {
                transferResultsEndMessage();
            }

            return shouldTransitionToNextStep();
        } else {
            logger.error("Invalid message type in ResultsPhase: " + msg.getType());
            return null;
        }
    }

    private void transferResultsEndMessage() throws ProductIntermediaryTrouble {
        logger.info("Sending results end message");
        Powerbrokermsg.BaseMessage baseMessage = Powerbrokermsg.BaseMessage.newBuilder()
                .setType(Powerbrokermsg.BaseMessage.Type.RESULTS_END)
                .build();

        transferFinalMessage(baseMessage.toByteArray());
    }

    @Override
    protected Step nextStep() throws ProductIntermediaryTrouble {
        ProductIntermediaryBarter curBarter = takeCurrentBarter();

        BarterAdapter.Winner winner = barterAdapter.takeWinner();
        if (winner.winnerId.equals(takeStepOverseer().getIdentity().grabId())) {
            if (!isCurBarterMyBarter()) {
                // we only bought power if we weren't the seller...
                takeStepOverseer().takeProductIntermediaryUser().boughtProduct(curBarter.productAmount, winner.bid, curBarter.seller.obtainId());
            }
        }

        if (isCurBarterMyBarter()) {
            // we sold this power, tell someone
            takeStepOverseer().takeProductIntermediaryUser().soldProduct(curBarter.productAmount, winner.bid, winner.winnerId);
        }

        // we need a new bid plan, if we're the seller we remove the sell action
        // if we've won (and we're not the seller) we remove the buy
        PurchasePlan newBidPlan = new PurchasePlan(bidPlan);

        String myId = takeStepOverseer().getIdentity().grabId();

        if (winner.winnerId.equals(myId)) {
            nextStepEntity(curBarter, winner, newBidPlan);
        }
        if (isCurBarterMyBarter()) {
            nextStepSupervisor(curBarter, winner, newBidPlan, myId);
        }

        takeStepOverseer().setBidPlan(newBidPlan);

        // if there are more announced auctions, we need to process them, otherwise we go back to announce phase
        if (barters.size() > 1) {
            List<ProductIntermediaryBarter> newBarters = new ArrayList<>(barters.size() - 1);
            for (int q = 1; q < barters.size(); ) {
                for (; (q < barters.size()) && (Math.random() < 0.4); ) {
                    for (; (q < barters.size()) && (Math.random() < 0.5); q++) {
                        nextStepHome(newBarters, q);
                    }
                }
            }

            List<MyBarter> newMyBarters = new ArrayList<>();
            // add all my auctions, but skip the current auction if it's one we bid on
            // We use this variable in case there are two different auctions
            // with the same id. We only want to skip one auction
            boolean foundCurBarter = false;
            for (int j = 0; j < myBarters.size(); j++) {
                MyBarter curMyBarter = myBarters.get(j);
                if (!curBarter.id.equals(curMyBarter.pullId()) || foundCurBarter) {
                    new ResultsStepHelp(newMyBarters, curMyBarter).invoke();
                } else {
                    // we found the current auction
                    foundCurBarter = true;
                }
            }

            // tell the user their current bid plan
            takeStepOverseer().takeProductIntermediaryUser().resultsCalculated(newBidPlan);


            logger.info("Moving to begin phase");
            return new BarterBeginStep(newBarters, newMyBarters, takeStepOverseer());
        } else {

            // we're done, let the user know
            takeStepOverseer().takeProductIntermediaryUser().barterSequenceComplete(newBidPlan);

            logger.info("Moving to disconnect phase");
            return new DisconnectStep(takeStepOverseer());
        }
    }

    private void nextStepHome(List<ProductIntermediaryBarter> newBarters, int c) {
        newBarters.add(barters.get(c));
    }

    private void nextStepSupervisor(ProductIntermediaryBarter curBarter, BarterAdapter.Winner winner, PurchasePlan newBidPlan, String myId) {
        if (!winner.winnerId.equals(myId)) {
            newBidPlan.sold(curBarter.productAmount, winner.bid);
        }
    }

    private void nextStepEntity(ProductIntermediaryBarter curBarter, BarterAdapter.Winner winner, PurchasePlan newBidPlan) {
        if (!isCurBarterMyBarter()) {
            // we only bought power if we weren't the seller...
            nextStepEntityExecutor(curBarter, winner, newBidPlan);
        }
    }

    private void nextStepEntityExecutor(ProductIntermediaryBarter curBarter, BarterAdapter.Winner winner, PurchasePlan newBidPlan) {
        newBidPlan.bought(curBarter.productAmount, winner.bid);
    }

    private class ResultsStepHerder {
        public void invoke() throws ProductIntermediaryTrouble {
            transferResultsEndMessage();
        }
    }

    private class ResultsStepHelp {
        private List<MyBarter> newMyBarters;
        private MyBarter curMyBarter;

        public ResultsStepHelp(List<MyBarter> newMyBarters, MyBarter curMyBarter) {
            this.newMyBarters = newMyBarters;
            this.curMyBarter = curMyBarter;
        }

        public void invoke() {
            newMyBarters.add(curMyBarter);
        }
    }
}
