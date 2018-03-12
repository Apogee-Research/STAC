package net.roboticapex.broker.period;

import net.roboticapex.senderReceivers.SenderReceiversConnection;
import net.roboticapex.senderReceivers.SenderReceiversDeviation;
import net.roboticapex.broker.BidPlan;
import net.roboticapex.broker.MyTrade;
import net.roboticapex.broker.ProductLiaisonTrade;
import net.roboticapex.broker.ProductLiaisonDeviation;
import net.roboticapex.broker.Powerbrokermsg;
import net.roboticapex.broker.selloff.TradeAdapter;
import net.roboticapex.logging.Logger;
import net.roboticapex.logging.LoggerFactory;

import java.util.List;

public class TradeBeginStep extends TradeBaseStep {

    private final Logger logger = LoggerFactory.fetchLogger(getClass());
    private final BidPlan promisePlan;
    private final TradeAdapter tradeAdapter;

    /**
     *
     * @param trades the entire list of auctions that we'll be processing at some point.  For the purposes of this
     *                 phase (and most others) we're only ever concerned with the first auction on this list. That's
     *                 the 'current' auction.
     * @param myTrades a list of auctions that I've started.
     * @param stepOverseer
     */
    public TradeBeginStep(List<ProductLiaisonTrade> trades, List<MyTrade> myTrades, StepOverseer stepOverseer) {
        super(trades, myTrades, stepOverseer);
        this.promisePlan = stepOverseer.getPromisePlan();
        this.tradeAdapter = new TradeAdapter(stepOverseer.fetchIdentity(), stepOverseer.obtainConnections());
    }

    @Override
    public void enterStep() throws ProductLiaisonDeviation {
        super.enterStep();

        // is this my auction or someone else's?
        // (we're only talking about the current auction)
        if (isItMyTurnToTransferMessages()) {
            if (isCurTradeMyTrade()) {
                startTrade();
            } else {
                enterStepHandler();
            }
        } else {
            logger.info("Not my turn to send message");
        }
    }

    private void enterStepHandler() throws ProductLiaisonDeviation {
        transferTradeBeginMsg();
    }

    @Override
    public Step handleMsg(SenderReceiversConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductLiaisonDeviation {

        if (msg.getType() == Powerbrokermsg.BaseMessage.Type.AUCTION_DATA) {
            try {
                tradeAdapter.handle(connection.obtainTheirIdentity(), msg.getAuctionData().toByteArray());
            } catch (SenderReceiversDeviation e) {
                throw new ProductLiaisonDeviation(e);
            }

            // we don't need to do any further processing...
            return null;
        }

        if (msg.getType() != Powerbrokermsg.BaseMessage.Type.AUCTION_BEGIN) {
            return handleMsgCoordinator(msg);
        }

        logger.info("received begin message from " + connection.obtainTheirIdentity().pullTruncatedId());

        addFinalMessage(connection.obtainTheirIdentity(), msg);

        String expectedId = pullCurrentTrade().id;
        if (!msg.getAuctionBegin().getAuctionId().equals(expectedId)) {
            return new TradeBeginStepTarget(msg, expectedId).invoke();
        }

        if (!hasSentFinalMessage) {
            if (isItMyTurnToTransferMessages()) {
                if (isCurTradeMyTrade()) {
                    startTrade();
                } else {
                    handleMsgGuide();
                }
            }
        }

        return shouldTransitionToNextStep();
    }

    private void handleMsgGuide() throws ProductLiaisonDeviation {
        new TradeBeginStepGuide().invoke();
    }

    private Step handleMsgCoordinator(Powerbrokermsg.BaseMessage msg) {
        logger.error("Invalid message type in AuctionBeginPhase: " + msg.getType());
        return null;
    }

    private void startTrade() throws ProductLiaisonDeviation {

        logger.info("Starting auction...");

        ProductLiaisonTrade curTrade = pullCurrentTrade();
        MyTrade myTrade = null;

        // find our matching auction
        // makes the assumption that *we* either won't have two auctions
        // for the same amount, or, if we do, that it doesn't
        // matter what order they're resolved in.
        for (int j = 0; j < myTrades.size(); j++) {
            MyTrade curMyTrade = myTrades.get(j);
            if (curMyTrade.pullAmountOfProduct() == curTrade.productAmount) {
                myTrade = curMyTrade;
                break;
            }
        }

        if (myTrade == null) {
            startTradeAid(curTrade);
        }

        // we're selling!
        logger.info("We're selling " + curTrade.productAmount + " units of power for at least " +
                myTrade.takeReserve());

        try {
            tradeAdapter.startTrade(myTrade.getId(), myTrade.toString());
        } catch (SenderReceiversDeviation e) {
            throw new ProductLiaisonDeviation(e);
        }

        transferTradeBeginMsg();
    }

    private void startTradeAid(ProductLiaisonTrade curTrade) throws ProductLiaisonDeviation {
        throw new ProductLiaisonDeviation("This is our auction but we have no record of it. PowerAmount: " +
                curTrade.productAmount);
    }

    private void transferTradeBeginMsg() throws ProductLiaisonDeviation {

        logger.info("Sending auction begin message...");

        // let the user know
        grabStepOverseer().fetchProductLiaisonUser().tradeStarted(pullCurrentTrade().id, pullCurrentTrade().productAmount);

        ProductLiaisonTrade curTrade = pullCurrentTrade();

        // We're going to send a message indicating we're going to join in the bidding on this auction.
        // Then we're going to wait for everyone to confirm this is the current auction.
        // (Which they'll do by just repeating the message back to everyone)
        Powerbrokermsg.AuctionBeginMessage beginMessage = Powerbrokermsg.AuctionBeginMessage.newBuilder()
                .setPowerAmount(curTrade.productAmount)
                .setAuctionId(curTrade.id)
                .build();

        Powerbrokermsg.BaseMessage baseMessage = Powerbrokermsg.BaseMessage.newBuilder()
                .setAuctionBegin(beginMessage)
                .setType(Powerbrokermsg.BaseMessage.Type.AUCTION_BEGIN)
                .build();

        transferFinalMessage(baseMessage.toByteArray());
    }

    protected Step nextStep() throws ProductLiaisonDeviation {
        logger.info("Moving to bidding phase");
        return new BiddingStep(trades, myTrades, tradeAdapter, grabStepOverseer());
    }

    private class TradeBeginStepTarget {
        private Powerbrokermsg.BaseMessage msg;
        private String expectedId;

        public TradeBeginStepTarget(Powerbrokermsg.BaseMessage msg, String expectedId) {
            this.msg = msg;
            this.expectedId = expectedId;
        }

        public Step invoke() throws ProductLiaisonDeviation {
            throw new ProductLiaisonDeviation("Unexpected auction id. Expected: [" + expectedId + "] got ["
                    + msg.getAuctionBegin().getAuctionId());
        }
    }

    private class TradeBeginStepGuide {
        public void invoke() throws ProductLiaisonDeviation {
            logger.info("We're observing this auction, we might bid");
            // send auction begin message and move on
            transferTradeBeginMsg();
        }
    }
}
