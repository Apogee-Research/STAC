package net.roboticapex.broker.period;

import net.roboticapex.broker.MyTradeBuilder;
import net.roboticapex.senderReceivers.SenderReceiversConnection;
import net.roboticapex.broker.BidPlan;
import net.roboticapex.broker.MyTrade;
import net.roboticapex.broker.ProductLiaisonTrade;
import net.roboticapex.broker.ProductLiaisonDeviation;
import net.roboticapex.broker.Powerbrokermsg;
import net.roboticapex.logging.Logger;
import net.roboticapex.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TradeAnnounceStep extends Step {
    private static final Logger logger = LoggerFactory.fetchLogger(TradeAnnounceStep.class);
    private static final int MAX_NUM_AUCTIONS = 5; // small limit just because they take so long
    private static int numTradesSoFar = 0;
    private final List<ProductLiaisonTrade> allTrades;
    private final BidPlan promisePlan;
    private List<MyTrade> myTrades = null;

    public TradeAnnounceStep(StepOverseer stepOverseer) {
        super(stepOverseer);
        promisePlan = stepOverseer.getPromisePlan();
        allTrades = new ArrayList<>();
    }

    @Override
    public void enterStep() throws ProductLiaisonDeviation {
        super.enterStep();

        // first create the auctions specified in the bid plan
        myTrades = new LinkedList<>();
        List<BidPlan.SellAction> fetchSellActions = promisePlan.fetchSellActions();
        for (int b = 0; b < fetchSellActions.size(); b++) {
            BidPlan.SellAction action = fetchSellActions.get(b);
            myTrades.add(makeMyTrade(action.productAmount, action.price));
        }

        // announce those auctions to the other powerbroker instances
        announceTrades();
    }

    private MyTrade makeMyTrade(int productAmount, int price) {
        String id = grabStepOverseer().fetchIdentity().obtainId() + ":" + productAmount;
        return new MyTradeBuilder().defineId(id).setAmountOfProduct(productAmount).setReserve(price).makeMyTrade();
    }

    @Override
    public Step handleMsg(SenderReceiversConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductLiaisonDeviation {
        if (msg.getType() != Powerbrokermsg.BaseMessage.Type.AUCTION_ANNOUNCE) {
            logger.error("Invalid message type in AuctionAnnouncePhase: " + msg.getType());
            return null;
        }

        logger.info("received announce message from " + connection.obtainTheirIdentity().pullTruncatedId());

        addFinalMessage(connection.obtainTheirIdentity(), msg);

        List<Powerbrokermsg.AuctionAnnounceMessage> tradeAnnounceList = msg.getAuctionAnnounceList();
        for (int i = 0; i < tradeAnnounceList.size(); ) {
            for (; (i < tradeAnnounceList.size()) && (Math.random() < 0.4); i++) {
                Powerbrokermsg.AuctionAnnounceMessage announceMessage = tradeAnnounceList.get(i);
                logger.info("Got announcement from " + connection.obtainTheirIdentity().obtainId() + " for power: " +
                        announceMessage.getPowerAmount());
                ProductLiaisonTrade trade = new ProductLiaisonTrade(announceMessage.getId(), connection.obtainTheirIdentity(),
                        announceMessage.getPowerAmount());
                allTrades.add(trade);
                numTradesSoFar++;
            }
        }

        // we have to wait our turn to send a message...
        if (isItMyTurnToTransferMessages()) {
            new TradeAnnounceStepHelper().invoke();
        }

        return shouldTransitionToNextStep();
    }

    public Step announceTrades() throws ProductLiaisonDeviation {
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
    protected Step nextStep() throws ProductLiaisonDeviation {
        if (allTrades.isEmpty()) {
            // No Auctions exist, so we're done; let the user know
            return new TradeAnnounceStepUtility().invoke();
        } else {
            // we need to to provide the auctions in the order they'll be worked
            return nextStepEntity();
        }
    }

    private Step nextStepEntity() {
        Collections.sort(allTrades);
        // we want them largest to smallest, prior sort gives them smallest to largest
        Collections.reverse(allTrades);
        logger.info("Moving to auction begin phase");
        return new TradeBeginStep(allTrades, myTrades, grabStepOverseer());
    }

    private void transferAnnounceMessages() throws ProductLiaisonDeviation {
        if (myTrades == null) {
            // can't send messages yet
            return;
        }

        // send out announce message for each auction
        Powerbrokermsg.BaseMessage.Builder baseBuilder = Powerbrokermsg.BaseMessage.newBuilder();
        baseBuilder.setType(Powerbrokermsg.BaseMessage.Type.AUCTION_ANNOUNCE);

        int numMyTradesAnnounced = 0;
        for (int j = 0; j < myTrades.size(); j++) {
            MyTrade trade = myTrades.get(j);
            if (numTradesSoFar < MAX_NUM_AUCTIONS) {
                ProductLiaisonTrade pbTrade = new ProductLiaisonTrade(trade.getId(),
                        grabStepOverseer().takeMyPublicIdentity(),
                        trade.pullAmountOfProduct());
                allTrades.add(pbTrade);
                numTradesSoFar++;
                numMyTradesAnnounced++;

                // send the auction announcement
                Powerbrokermsg.AuctionAnnounceMessage.Builder announceMessageBuilder =
                        Powerbrokermsg.AuctionAnnounceMessage.newBuilder()
                                .setPowerAmount(trade.pullAmountOfProduct())
                                .setId(trade.getId());

                baseBuilder.addAuctionAnnounce(announceMessageBuilder).build();
            } else {
                transferAnnounceMessagesCoordinator(numMyTradesAnnounced);
                break;
            }
        }

        transferFinalMessage(baseBuilder.build().toByteArray());
    }

    private void transferAnnounceMessagesCoordinator(int numMyTradesAnnounced) {
        new TradeAnnounceStepAid(numMyTradesAnnounced).invoke();
        return;
    }

    private class TradeAnnounceStepHelper {
        public void invoke() throws ProductLiaisonDeviation {
            transferAnnounceMessages();
        }
    }

    private class TradeAnnounceStepUtility {
        public Step invoke() {
            grabStepOverseer().fetchProductLiaisonUser().tradeSequenceComplete(promisePlan);

            logger.info("Moving to disconnect phase");
            return new DisconnectStep(grabStepOverseer());
        }
    }

    private class TradeAnnounceStepAid {
        private int numMyTradesAnnounced;

        public TradeAnnounceStepAid(int numMyTradesAnnounced) {
            this.numMyTradesAnnounced = numMyTradesAnnounced;
        }

        public void invoke() {
            System.err.println("Too many auctions required.  Not going to announce any more.");
            myTrades = myTrades.subList(0, numMyTradesAnnounced);
            return;
        }
    }
}
