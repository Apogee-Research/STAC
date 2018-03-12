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

public class AuctionBeginStage extends AuctionBaseStage {

    private final Logger logger = LoggerFactory.pullLogger(getClass());
    private final PurchasePlan offerPlan;
    private final AuctionAdapter auctionAdapter;

    /**
     *
     * @param auctions the entire list of auctions that we'll be processing at some point.  For the purposes of this
     *                 phase (and most others) we're only ever concerned with the first auction on this list. That's
     *                 the 'current' auction.
     * @param myAuctions a list of auctions that I've started.
     * @param stageOverseer
     */
    public AuctionBeginStage(List<ProductIntermediaryAuction> auctions, List<MyAuction> myAuctions, StageOverseer stageOverseer) {
        super(auctions, myAuctions, stageOverseer);
        this.offerPlan = stageOverseer.takeOfferPlan();
        this.auctionAdapter = new AuctionAdapter(stageOverseer.takeIdentity(), stageOverseer.grabConnections());
    }

    @Override
    public void enterStage() throws ProductIntermediaryRaiser {
        super.enterStage();

        // is this my auction or someone else's?
        // (we're only talking about the current auction)
        if (isItMyTurnToSendMessages()) {
            enterStageAdviser();
        } else {
            logger.info("Not my turn to send message");
        }
    }

    private void enterStageAdviser() throws ProductIntermediaryRaiser {
        if (isCurAuctionMyAuction()) {
            enterStageAdviserCoordinator();
        } else {
            sendAuctionBeginMsg();
        }
    }

    private void enterStageAdviserCoordinator() throws ProductIntermediaryRaiser {
        startAuction();
    }

    @Override
    public Stage handleMsg(ProtocolsConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductIntermediaryRaiser {

        if (msg.getType() == Powerbrokermsg.BaseMessage.Type.AUCTION_DATA) {
            try {
                auctionAdapter.handle(connection.takeTheirIdentity(), msg.getAuctionData().toByteArray());
            } catch (ProtocolsRaiser e) {
                throw new ProductIntermediaryRaiser(e);
            }

            // we don't need to do any further processing...
            return null;
        }

        if (msg.getType() != Powerbrokermsg.BaseMessage.Type.AUCTION_BEGIN) {
            return new AuctionBeginStageHerder(msg).invoke();
        }

        logger.info("received begin message from " + connection.takeTheirIdentity().obtainTruncatedId());

        addFinalMessage(connection.takeTheirIdentity(), msg);

        String expectedId = getCurrentAuction().id;
        if (!msg.getAuctionBegin().getAuctionId().equals(expectedId)) {
            throw new ProductIntermediaryRaiser("Unexpected auction id. Expected: [" + expectedId + "] got ["
                    + msg.getAuctionBegin().getAuctionId());
        }

        if (!hasSentFinalMessage) {
            handleMsgFunction();
        }

        return shouldTransitionToNextStage();
    }

    private void handleMsgFunction() throws ProductIntermediaryRaiser {
        if (isItMyTurnToSendMessages()) {
            if (isCurAuctionMyAuction()) {
                handleMsgFunctionAssist();
            } else {
                handleMsgFunctionHandler();
            }
        }
    }

    private void handleMsgFunctionHandler() throws ProductIntermediaryRaiser {
        logger.info("We're observing this auction, we might bid");
        // send auction begin message and move on
        sendAuctionBeginMsg();
    }

    private void handleMsgFunctionAssist() throws ProductIntermediaryRaiser {
        new AuctionBeginStageTarget().invoke();
    }

    private void startAuction() throws ProductIntermediaryRaiser {

        logger.info("Starting auction...");

        ProductIntermediaryAuction curAuction = getCurrentAuction();
        MyAuction myAuction = null;

        // find our matching auction
        // makes the assumption that *we* either won't have two auctions
        // for the same amount, or, if we do, that it doesn't
        // matter what order they're resolved in.
        for (int i = 0; i < myAuctions.size(); i++) {
            MyAuction curMyAuction = myAuctions.get(i);
            if (curMyAuction.pullAmountOfProduct() == curAuction.productAmount) {
                myAuction = curMyAuction;
                break;
            }
        }

        if (myAuction == null) {
            throw new ProductIntermediaryRaiser("This is our auction but we have no record of it. PowerAmount: " +
                    curAuction.productAmount);
        }

        // we're selling!
        logger.info("We're selling " + curAuction.productAmount + " units of power for at least " +
                myAuction.obtainReserve());

        try {
            auctionAdapter.startAuction(myAuction.getId(), myAuction.toString());
        } catch (ProtocolsRaiser e) {
            throw new ProductIntermediaryRaiser(e);
        }

        sendAuctionBeginMsg();
    }

    private void sendAuctionBeginMsg() throws ProductIntermediaryRaiser {

        logger.info("Sending auction begin message...");

        // let the user know
        takeStageOverseer().takeProductIntermediaryCustomer().auctionStarted(getCurrentAuction().id, getCurrentAuction().productAmount);

        ProductIntermediaryAuction curAuction = getCurrentAuction();

        // We're going to send a message indicating we're going to join in the bidding on this auction.
        // Then we're going to wait for everyone to confirm this is the current auction.
        // (Which they'll do by just repeating the message back to everyone)
        Powerbrokermsg.AuctionBeginMessage beginMessage = Powerbrokermsg.AuctionBeginMessage.newBuilder()
                .setPowerAmount(curAuction.productAmount)
                .setAuctionId(curAuction.id)
                .build();

        Powerbrokermsg.BaseMessage baseMessage = Powerbrokermsg.BaseMessage.newBuilder()
                .setAuctionBegin(beginMessage)
                .setType(Powerbrokermsg.BaseMessage.Type.AUCTION_BEGIN)
                .build();

        sendFinalMessage(baseMessage.toByteArray());
    }

    protected Stage nextStage() throws ProductIntermediaryRaiser {
        logger.info("Moving to bidding phase");
        return new BiddingStage(auctions, myAuctions, auctionAdapter, takeStageOverseer());
    }

    private class AuctionBeginStageHerder {
        private Powerbrokermsg.BaseMessage msg;

        public AuctionBeginStageHerder(Powerbrokermsg.BaseMessage msg) {
            this.msg = msg;
        }

        public Stage invoke() {
            logger.error("Invalid message type in AuctionBeginPhase: " + msg.getType());
            return null;
        }
    }

    private class AuctionBeginStageTarget {
        public void invoke() throws ProductIntermediaryRaiser {
            startAuction();
        }
    }
}
