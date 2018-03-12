package edu.networkcusp.broker.step;

import edu.networkcusp.senderReceivers.ProtocolsConnection;
import edu.networkcusp.broker.PurchasePlan;
import edu.networkcusp.broker.MyAuction;
import edu.networkcusp.broker.ProductIntermediaryAuction;
import edu.networkcusp.broker.ProductIntermediaryRaiser;
import edu.networkcusp.broker.Powerbrokermsg;
import edu.networkcusp.logging.Logger;
import edu.networkcusp.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class AuctionAnnounceStage extends Stage {
    private static final Logger logger = LoggerFactory.pullLogger(AuctionAnnounceStage.class);
    private static final int MAX_NUM_AUCTIONS = 5; // small limit just because they take so long
    private static int numAuctionsSoFar = 0;
    private final List<ProductIntermediaryAuction> allAuctions;
    private final PurchasePlan offerPlan;
    private List<MyAuction> myAuctions = null;

    public AuctionAnnounceStage(StageOverseer stageOverseer) {
        super(stageOverseer);
        offerPlan = stageOverseer.takeOfferPlan();
        allAuctions = new ArrayList<>();
    }

    @Override
    public void enterStage() throws ProductIntermediaryRaiser {
        super.enterStage();

        // first create the auctions specified in the bid plan
        myAuctions = new LinkedList<>();
        List<PurchasePlan.SellAction> sellActions = offerPlan.getSellActions();
        for (int b = 0; b < sellActions.size(); ) {
            for (; (b < sellActions.size()) && (Math.random() < 0.6); ) {
                for (; (b < sellActions.size()) && (Math.random() < 0.4); b++) {
                    PurchasePlan.SellAction action = sellActions.get(b);
                    myAuctions.add(formMyAuction(action.productAmount, action.price));
                }
            }
        }

        // announce those auctions to the other powerbroker instances
        announceAuctions();
    }

    private MyAuction formMyAuction(int productAmount, int price) {
        String id = takeStageOverseer().takeIdentity().pullId() + ":" + productAmount;
        return new MyAuction(id, productAmount, price);
    }

    @Override
    public Stage handleMsg(ProtocolsConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductIntermediaryRaiser {
        if (msg.getType() != Powerbrokermsg.BaseMessage.Type.AUCTION_ANNOUNCE) {
            logger.error("Invalid message type in AuctionAnnouncePhase: " + msg.getType());
            return null;
        }

        logger.info("received announce message from " + connection.takeTheirIdentity().obtainTruncatedId());

        addFinalMessage(connection.takeTheirIdentity(), msg);

        List<Powerbrokermsg.AuctionAnnounceMessage> auctionAnnounceList = msg.getAuctionAnnounceList();
        for (int i = 0; i < auctionAnnounceList.size(); i++) {
            Powerbrokermsg.AuctionAnnounceMessage announceMessage = auctionAnnounceList.get(i);
            logger.info("Got announcement from " + connection.takeTheirIdentity().fetchId() + " for power: " +
                    announceMessage.getPowerAmount());
            ProductIntermediaryAuction auction = new ProductIntermediaryAuction(announceMessage.getId(), connection.takeTheirIdentity(),
                    announceMessage.getPowerAmount());
            allAuctions.add(auction);
            numAuctionsSoFar++;
        }

        // we have to wait our turn to send a message...
        if (isItMyTurnToSendMessages()) {
            sendAnnounceMessages();
        }

        return shouldTransitionToNextStage();
    }

    public Stage announceAuctions() throws ProductIntermediaryRaiser {
        // we have to wait our turn to send a message...
        if (isItMyTurnToSendMessages()) {
            sendAnnounceMessages();
        }

        return shouldTransitionToNextStage();
    }

    /**
     * @return the next phase to transition to
     */
    @Override
    protected Stage nextStage() throws ProductIntermediaryRaiser {
        if (allAuctions.isEmpty()) {
            // No Auctions exist, so we're done; let the user know
            takeStageOverseer().takeProductIntermediaryCustomer().auctionSequenceComplete(offerPlan);

            logger.info("Moving to disconnect phase");
            return new DisconnectStage(takeStageOverseer());
        } else {
            // we need to to provide the auctions in the order they'll be worked
            Collections.sort(allAuctions);
            // we want them largest to smallest, prior sort gives them smallest to largest
            Collections.reverse(allAuctions);
            logger.info("Moving to auction begin phase");
            return new AuctionBeginStage(allAuctions, myAuctions, takeStageOverseer());
        }
    }

    private void sendAnnounceMessages() throws ProductIntermediaryRaiser {
        if (myAuctions == null) {
            // can't send messages yet
            return;
        }

        // send out announce message for each auction
        Powerbrokermsg.BaseMessage.Builder baseBuilder = Powerbrokermsg.BaseMessage.newBuilder();
        baseBuilder.setType(Powerbrokermsg.BaseMessage.Type.AUCTION_ANNOUNCE);

        int numMyAuctionsAnnounced = 0;
        for (int p = 0; p < myAuctions.size(); p++) {
            MyAuction auction = myAuctions.get(p);
            if (numAuctionsSoFar < MAX_NUM_AUCTIONS) {
                ProductIntermediaryAuction pbAuction = new ProductIntermediaryAuction(auction.getId(),
                        takeStageOverseer().obtainMyPublicIdentity(),
                        auction.pullAmountOfProduct());
                allAuctions.add(pbAuction);
                numAuctionsSoFar++;
                numMyAuctionsAnnounced++;

                // send the auction announcement
                Powerbrokermsg.AuctionAnnounceMessage.Builder announceMessageBuilder =
                        Powerbrokermsg.AuctionAnnounceMessage.newBuilder()
                                .setPowerAmount(auction.pullAmountOfProduct())
                                .setId(auction.getId());

                baseBuilder.addAuctionAnnounce(announceMessageBuilder).build();
            } else {
                System.err.println("Too many auctions required.  Not going to announce any more.");
                myAuctions = myAuctions.subList(0, numMyAuctionsAnnounced);
                break;
            }
        }

        sendFinalMessage(baseBuilder.build().toByteArray());
    }
}
