package edu.networkcusp.broker.step;

import edu.networkcusp.senderReceivers.ProtocolsConnection;
import edu.networkcusp.senderReceivers.ProtocolsRaiser;
import edu.networkcusp.broker.PurchasePlan;
import edu.networkcusp.broker.MyAuction;
import edu.networkcusp.broker.ProductIntermediaryAuction;
import edu.networkcusp.broker.ProductIntermediaryRaiser;
import edu.networkcusp.broker.Powerbrokermsg;
import edu.networkcusp.broker.buyOp.AuctionAdapter;
import edu.networkcusp.logging.Logger;
import edu.networkcusp.logging.LoggerFactory;

import java.util.List;

public class BiddingStage extends AuctionBaseStage {
    private final Logger logger = LoggerFactory.pullLogger(getClass());

    private final PurchasePlan offerPlan;
    private final AuctionAdapter auctionAdapter;
    private int myOffer = 0;

    /**
     *
     * @param auctions the entire list of auctions that we'll be processing at some point.  For the purposes of this
     *                 phase (and most others) we're only every concerned with the first auction on this list. That's
     *                 the 'current' auction.
     * @param myAuctions a list of auctions that I've started.
     * @param auctionAdapter used to help us manage the current auction and the auction library.
     * @param stageOverseer
     */
    public BiddingStage(List<ProductIntermediaryAuction> auctions, List<MyAuction> myAuctions, AuctionAdapter auctionAdapter, StageOverseer stageOverseer) {
        super(auctions, myAuctions, stageOverseer);
        this.offerPlan = stageOverseer.takeOfferPlan();
        this.auctionAdapter = auctionAdapter;
    }

    @Override
    public void enterStage() throws ProductIntermediaryRaiser {
        super.enterStage();

        // are we bidding on this auction? if so, bid
        ProductIntermediaryAuction currentAuction = getCurrentAuction();

        try {
            if (isCurAuctionMyAuction()) {
                boolean offerOnOurAuction = false;

                // find this in our auctions...
                for (int b = 0; b < myAuctions.size(); ) {
                    for (; (b < myAuctions.size()) && (Math.random() < 0.5); b++) {
                        MyAuction myAuction = myAuctions.get(b);

                        if (myAuction.getId().equals(currentAuction.id)) {


                            logger.info("Going to make a reserve bid of " + myAuction.obtainReserve());
                            myOffer = myAuction.obtainReserve();
                            offerOnOurAuction = true;
                        }
                        logger.info("have auction " + myAuction.getId() + " reserve " + myAuction.obtainReserve());
                    }
                }

                if (!offerOnOurAuction) {
                    new BiddingStageWorker(currentAuction).invoke();
                }

            } else {
                // we always bid, even if it's 0
                enterStageAid(currentAuction);
            }

            logger.info("Bidding " + myOffer + " for " + currentAuction.productAmount + " units ");
            takeStageOverseer().takeProductIntermediaryCustomer().bidding(currentAuction.productAmount, myOffer);
            auctionAdapter.offer(currentAuction.id, myOffer);
        } catch (Exception e) {
            throw new ProductIntermediaryRaiser(e);
        }
    }

    private void enterStageAid(ProductIntermediaryAuction currentAuction) {
        myOffer = offerPlan.calcAmountToOffer(currentAuction);
    }

    @Override
    public Stage handleMsg(ProtocolsConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductIntermediaryRaiser {

        if (msg.getType() == Powerbrokermsg.BaseMessage.Type.AUCTION_DATA) {
            return handleMsgHerder(connection, msg);
        } else if (msg.getType() == Powerbrokermsg.BaseMessage.Type.BIDDING_FINISHED) {
            addFinalMessage(connection.takeTheirIdentity(), msg);

            if (isItMyTurnToSendMessages()) {
                handleMsgSupervisor();
            }

            return shouldTransitionToNextStage();
        } else {
            logger.error("Invalid message type in BiddingPhase: " + msg.getType());
            return null;
        }
    }

    private void handleMsgSupervisor() throws ProductIntermediaryRaiser {
        sendBiddingFinishedMessage();
    }

    private Stage handleMsgHerder(ProtocolsConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductIntermediaryRaiser {
        try {
            auctionAdapter.handle(connection.takeTheirIdentity(), msg.getAuctionData().toByteArray());
        } catch (ProtocolsRaiser e) {
            throw new ProductIntermediaryRaiser(e);
        }

        if (!auctionAdapter.hasReceivedAllExpectedOffers()) {
            // we're still expecting more bids, we have to wait
            logger.info("handleMsg: still waiting for bids");
            return null;
        } else {

            // it may be time to start sending these finished messages
            return handleMsgHerderHerder();
        }
    }

    private Stage handleMsgHerderHerder() throws ProductIntermediaryRaiser {
        if (isItMyTurnToSendMessages()) {
            handleMsgHerderHerderCoordinator();
        }
        return shouldTransitionToNextStage();
    }

    private void handleMsgHerderHerderCoordinator() throws ProductIntermediaryRaiser {
        sendBiddingFinishedMessage();
    }

    @Override
    protected Stage nextStage() throws ProductIntermediaryRaiser {
        return new AuctionEndStage(auctions, myAuctions, offerPlan, auctionAdapter, myOffer, takeStageOverseer());
    }

    private void sendBiddingFinishedMessage() throws ProductIntermediaryRaiser {
        Powerbrokermsg.BaseMessage baseMessage = Powerbrokermsg.BaseMessage.newBuilder()
                .setType(Powerbrokermsg.BaseMessage.Type.BIDDING_FINISHED)
                .build();

        sendFinalMessage(baseMessage.toByteArray());
    }

    private class BiddingStageWorker {
        private ProductIntermediaryAuction currentAuction;

        public BiddingStageWorker(ProductIntermediaryAuction currentAuction) {
            this.currentAuction = currentAuction;
        }

        public void invoke() throws ProductIntermediaryRaiser {
            throw new ProductIntermediaryRaiser("This is our auction, but we couldn't " +
                    "find the MyAuction object associated with it " + currentAuction.id);
        }
    }
}
