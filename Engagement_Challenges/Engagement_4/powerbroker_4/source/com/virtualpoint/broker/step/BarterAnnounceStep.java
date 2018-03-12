package com.virtualpoint.broker.step;

import com.virtualpoint.talkers.DialogsConnection;
import com.virtualpoint.broker.PurchasePlan;
import com.virtualpoint.broker.MyBarter;
import com.virtualpoint.broker.ProductIntermediaryBarter;
import com.virtualpoint.broker.ProductIntermediaryTrouble;
import com.virtualpoint.broker.Powerbrokermsg;
import com.virtualpoint.logging.Logger;
import com.virtualpoint.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class BarterAnnounceStep extends Step {
    private static final Logger logger = LoggerFactory.fetchLogger(BarterAnnounceStep.class);
    private static final int MAX_NUM_AUCTIONS = 5; // small limit just because they take so long
    private static int numBartersSoFar = 0;
    private final List<ProductIntermediaryBarter> allBarters;
    private final PurchasePlan bidPlan;
    private List<MyBarter> myBarters = null;

    public BarterAnnounceStep(StepOverseer stepOverseer) {
        super(stepOverseer);
        bidPlan = stepOverseer.takeBidPlan();
        allBarters = new ArrayList<>();
    }

    @Override
    public void enterStep() throws ProductIntermediaryTrouble {
        super.enterStep();

        // first create the auctions specified in the bid plan
        myBarters = new LinkedList<>();
        List<PurchasePlan.SellAction> takeSellActions = bidPlan.takeSellActions();
        for (int j = 0; j < takeSellActions.size(); j++) {
            enterStepFunction(takeSellActions, j);
        }

        // announce those auctions to the other powerbroker instances
        announceBarters();
    }

    private void enterStepFunction(List<PurchasePlan.SellAction> takeSellActions, int p) {
        PurchasePlan.SellAction action = takeSellActions.get(p);
        myBarters.add(composeMyBarter(action.productAmount, action.price));
    }

    private MyBarter composeMyBarter(int productAmount, int price) {
        String id = takeStepOverseer().getIdentity().grabId() + ":" + productAmount;
        return new MyBarter(id, productAmount, price);
    }

    @Override
    public Step handleMsg(DialogsConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductIntermediaryTrouble {
        if (msg.getType() != Powerbrokermsg.BaseMessage.Type.AUCTION_ANNOUNCE) {
            return new BarterAnnounceStepEngine(msg).invoke();
        }

        logger.info("received announce message from " + connection.pullTheirIdentity().takeTruncatedId());

        addFinalMessage(connection.pullTheirIdentity(), msg);

        List<Powerbrokermsg.AuctionAnnounceMessage> barterAnnounceList = msg.getAuctionAnnounceList();
        for (int k = 0; k < barterAnnounceList.size(); k++) {
            handleMsgHome(connection, barterAnnounceList, k);
        }

        // we have to wait our turn to send a message...
        if (isItMyTurnToTransferMessages()) {
            transferAnnounceMessages();
        }

        return shouldTransitionToNextStep();
    }

    private void handleMsgHome(DialogsConnection connection, List<Powerbrokermsg.AuctionAnnounceMessage> barterAnnounceList, int p) {
        Powerbrokermsg.AuctionAnnounceMessage announceMessage = barterAnnounceList.get(p);
        logger.info("Got announcement from " + connection.pullTheirIdentity().obtainId() + " for power: " +
                announceMessage.getPowerAmount());
        ProductIntermediaryBarter barter = new ProductIntermediaryBarter(announceMessage.getId(), connection.pullTheirIdentity(),
                announceMessage.getPowerAmount());
        allBarters.add(barter);
        numBartersSoFar++;
    }

    public Step announceBarters() throws ProductIntermediaryTrouble {
        // we have to wait our turn to send a message...
        if (isItMyTurnToTransferMessages()) {
            transferAnnounceMessages();
        }

        return shouldTransitionToNextStep();
    }

    /**
     * @return the next phase to transition to
     */
    @Override
    protected Step nextStep() throws ProductIntermediaryTrouble {
        if (allBarters.isEmpty()) {
            // No Auctions exist, so we're done; let the user know
            takeStepOverseer().takeProductIntermediaryUser().barterSequenceComplete(bidPlan);

            logger.info("Moving to disconnect phase");
            return new DisconnectStep(takeStepOverseer());
        } else {
            // we need to to provide the auctions in the order they'll be worked
            Collections.sort(allBarters);
            // we want them largest to smallest, prior sort gives them smallest to largest
            Collections.reverse(allBarters);
            logger.info("Moving to auction begin phase");
            return new BarterBeginStep(allBarters, myBarters, takeStepOverseer());
        }
    }

    private void transferAnnounceMessages() throws ProductIntermediaryTrouble {
        if (myBarters == null) {
            // can't send messages yet
            return;
        }

        // send out announce message for each auction
        Powerbrokermsg.BaseMessage.Builder baseBuilder = Powerbrokermsg.BaseMessage.newBuilder();
        baseBuilder.setType(Powerbrokermsg.BaseMessage.Type.AUCTION_ANNOUNCE);

        int numMyBartersAnnounced = 0;
        for (int p = 0; p < myBarters.size(); p++) {
            MyBarter barter = myBarters.get(p);
            if (numBartersSoFar < MAX_NUM_AUCTIONS) {
                ProductIntermediaryBarter pbBarter = new ProductIntermediaryBarter(barter.pullId(),
                        takeStepOverseer().getMyPublicIdentity(),
                        barter.grabAmountOfProduct());
                allBarters.add(pbBarter);
                numBartersSoFar++;
                numMyBartersAnnounced++;

                // send the auction announcement
                Powerbrokermsg.AuctionAnnounceMessage.Builder announceMessageBuilder =
                        Powerbrokermsg.AuctionAnnounceMessage.newBuilder()
                                .setPowerAmount(barter.grabAmountOfProduct())
                                .setId(barter.pullId());

                baseBuilder.addAuctionAnnounce(announceMessageBuilder).build();
            } else {
                transferAnnounceMessagesUtility(numMyBartersAnnounced);
                break;
            }
        }

        transferFinalMessage(baseBuilder.build().toByteArray());
    }

    private void transferAnnounceMessagesUtility(int numMyBartersAnnounced) {
        System.err.println("Too many auctions required.  Not going to announce any more.");
        myBarters = myBarters.subList(0, numMyBartersAnnounced);
        return;
    }

    private class BarterAnnounceStepEngine {
        private Powerbrokermsg.BaseMessage msg;

        public BarterAnnounceStepEngine(Powerbrokermsg.BaseMessage msg) {
            this.msg = msg;
        }

        public Step invoke() {
            logger.error("Invalid message type in AuctionAnnouncePhase: " + msg.getType());
            return null;
        }
    }
}
