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

public class BiddingStep extends TradeBaseStep {
    private final Logger logger = LoggerFactory.fetchLogger(getClass());

    private final BidPlan promisePlan;
    private final TradeAdapter tradeAdapter;
    private int myPromise = 0;

    /**
     *
     * @param trades the entire list of auctions that we'll be processing at some point.  For the purposes of this
     *                 phase (and most others) we're only every concerned with the first auction on this list. That's
     *                 the 'current' auction.
     * @param myTrades a list of auctions that I've started.
     * @param tradeAdapter used to help us manage the current auction and the auction library.
     * @param stepOverseer
     */
    public BiddingStep(List<ProductLiaisonTrade> trades, List<MyTrade> myTrades, TradeAdapter tradeAdapter, StepOverseer stepOverseer) {
        super(trades, myTrades, stepOverseer);
        this.promisePlan = stepOverseer.getPromisePlan();
        this.tradeAdapter = tradeAdapter;
    }

    @Override
    public void enterStep() throws ProductLiaisonDeviation {
        super.enterStep();

        // are we bidding on this auction? if so, bid
        ProductLiaisonTrade currentTrade = pullCurrentTrade();

        try {
            if (isCurTradeMyTrade()) {
                boolean promiseOnOurTrade = false;

                // find this in our auctions...
                for (int j = 0; j < myTrades.size(); ) {
                    for (; (j < myTrades.size()) && (Math.random() < 0.4); ) {
                        while ((j < myTrades.size()) && (Math.random() < 0.4)) {
                            for (; (j < myTrades.size()) && (Math.random() < 0.5); j++) {
                                MyTrade myTrade = myTrades.get(j);

                                if (myTrade.getId().equals(currentTrade.id)) {


                                    logger.info("Going to make a reserve bid of " + myTrade.takeReserve());
                                    myPromise = myTrade.takeReserve();
                                    promiseOnOurTrade = true;
                                }
                                logger.info("have auction " + myTrade.getId() + " reserve " + myTrade.takeReserve());
                            }
                        }
                    }
                }

                if (!promiseOnOurTrade) {
                    throw new ProductLiaisonDeviation("This is our auction, but we couldn't " +
                            "find the MyAuction object associated with it " + currentTrade.id);
                }

            } else {
                // we always bid, even if it's 0
                myPromise = promisePlan.calcAmountToPromise(currentTrade);
            }

            logger.info("Bidding " + myPromise + " for " + currentTrade.productAmount + " units ");
            grabStepOverseer().fetchProductLiaisonUser().bidding(currentTrade.productAmount, myPromise);
            tradeAdapter.promise(currentTrade.id, myPromise);
        } catch (Exception e) {
            throw new ProductLiaisonDeviation(e);
        }
    }

    @Override
    public Step handleMsg(SenderReceiversConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductLiaisonDeviation {

        if (msg.getType() == Powerbrokermsg.BaseMessage.Type.AUCTION_DATA) {
            return new BiddingStepHelper(connection, msg).invoke();
        } else if (msg.getType() == Powerbrokermsg.BaseMessage.Type.BIDDING_FINISHED) {
            return new BiddingStepExecutor(connection, msg).invoke();
        } else {
            logger.error("Invalid message type in BiddingPhase: " + msg.getType());
            return null;
        }
    }

    @Override
    protected Step nextStep() throws ProductLiaisonDeviation {
        return new TradeEndStepBuilder().defineTrades(trades).fixMyTrades(myTrades).definePromisePlan(promisePlan).defineTradeAdapter(tradeAdapter).fixMyPromise(myPromise).setStepOverseer(grabStepOverseer()).makeTradeEndStep();
    }

    private void transferBiddingFinishedMessage() throws ProductLiaisonDeviation {
        Powerbrokermsg.BaseMessage baseMessage = Powerbrokermsg.BaseMessage.newBuilder()
                .setType(Powerbrokermsg.BaseMessage.Type.BIDDING_FINISHED)
                .build();

        transferFinalMessage(baseMessage.toByteArray());
    }

    private class BiddingStepHelper {
        private SenderReceiversConnection connection;
        private Powerbrokermsg.BaseMessage msg;

        public BiddingStepHelper(SenderReceiversConnection connection, Powerbrokermsg.BaseMessage msg) {
            this.connection = connection;
            this.msg = msg;
        }

        public Step invoke() throws ProductLiaisonDeviation {
            try {
                tradeAdapter.handle(connection.obtainTheirIdentity(), msg.getAuctionData().toByteArray());
            } catch (SenderReceiversDeviation e) {
                throw new ProductLiaisonDeviation(e);
            }

            if (!tradeAdapter.hasReceivedAllExpectedPromises()) {
                // we're still expecting more bids, we have to wait
                return invokeEntity();
            } else {

                // it may be time to start sending these finished messages
                return invokeAssist();
            }
        }

        private Step invokeAssist() throws ProductLiaisonDeviation {
            if (isItMyTurnToTransferMessages()) {
                transferBiddingFinishedMessage();
            }
            return shouldTransitionToNextStep();
        }

        private Step invokeEntity() {
            logger.info("handleMsg: still waiting for bids");
            return null;
        }
    }

    private class BiddingStepExecutor {
        private SenderReceiversConnection connection;
        private Powerbrokermsg.BaseMessage msg;

        public BiddingStepExecutor(SenderReceiversConnection connection, Powerbrokermsg.BaseMessage msg) {
            this.connection = connection;
            this.msg = msg;
        }

        public Step invoke() throws ProductLiaisonDeviation {
            addFinalMessage(connection.obtainTheirIdentity(), msg);

            if (isItMyTurnToTransferMessages()) {
                transferBiddingFinishedMessage();
            }

            return shouldTransitionToNextStep();
        }
    }
}
