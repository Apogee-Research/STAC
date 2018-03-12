package net.roboticapex.broker.period;

import net.roboticapex.senderReceivers.SenderReceiversConnection;
import net.roboticapex.broker.BidPlan;
import net.roboticapex.broker.MyTrade;
import net.roboticapex.broker.ProductLiaisonTrade;
import net.roboticapex.broker.ProductLiaisonDeviation;
import net.roboticapex.broker.Powerbrokermsg;
import net.roboticapex.broker.selloff.TradeAdapter;
import net.roboticapex.logging.Logger;
import net.roboticapex.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ResultsStep extends TradeBaseStep {

    private final Logger logger = LoggerFactory.fetchLogger(getClass());

    private final BidPlan promisePlan;
    private final TradeAdapter tradeAdapter;

    /**
     *
     * @param trades the entire list of auctions that we'll be processing at some point.  For the purposes of this
     *                 phase (and most others) we're only every concerned with the first auction on this list. That's
     *                 the 'current' auction.
     * @param myTrades a list of auctions that I've started.
     * @param promisePlan the bidding plan that indicates what we should try to do.
     * @param tradeAdapter used to help us manage the current auction and the auction library.
     * @param stepOverseer
     */
    public ResultsStep(List<ProductLiaisonTrade> trades, List<MyTrade> myTrades, BidPlan promisePlan, TradeAdapter tradeAdapter, StepOverseer stepOverseer) {
        super(trades, myTrades, stepOverseer);
        this.promisePlan = promisePlan;
        this.tradeAdapter = tradeAdapter;
    }

    @Override
    public void enterStep() throws ProductLiaisonDeviation {
        super.enterStep();

        TradeAdapter.Winner winner = tradeAdapter.obtainWinner();

        if (winner.winnerId.equals(grabStepOverseer().fetchIdentity().obtainId())) {
            logger.info("I won. bid: " + winner.promise);
        } else {
            String id = winner.winnerId;
            if (id.length() > 25){
                id = id.substring(0, 25) + "...";
            }
            logger.info(id + " won. bid: " + winner.promise);
        }

        if (isItMyTurnToTransferMessages()) {
            transferResultsEndMessage();
        }
    }

    @Override
    public Step handleMsg(SenderReceiversConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductLiaisonDeviation {
        if (msg.getType() == Powerbrokermsg.BaseMessage.Type.RESULTS_END) {

            logger.info("recevied RESULTS_END from " + connection.obtainTheirIdentity().pullTruncatedId());

            addFinalMessage(connection.obtainTheirIdentity(), msg);

            if (isItMyTurnToTransferMessages()) {
                transferResultsEndMessage();
            }

            return shouldTransitionToNextStep();
        } else {
            return handleMsgGuide(msg);
        }
    }

    private Step handleMsgGuide(Powerbrokermsg.BaseMessage msg) {
        logger.error("Invalid message type in ResultsPhase: " + msg.getType());
        return null;
    }

    private void transferResultsEndMessage() throws ProductLiaisonDeviation {
        logger.info("Sending results end message");
        Powerbrokermsg.BaseMessage baseMessage = Powerbrokermsg.BaseMessage.newBuilder()
                .setType(Powerbrokermsg.BaseMessage.Type.RESULTS_END)
                .build();

        transferFinalMessage(baseMessage.toByteArray());
    }

    @Override
    protected Step nextStep() throws ProductLiaisonDeviation {
        ProductLiaisonTrade curTrade = pullCurrentTrade();

        TradeAdapter.Winner winner = tradeAdapter.obtainWinner();
        if (winner.winnerId.equals(grabStepOverseer().fetchIdentity().obtainId())) {
            if (!isCurTradeMyTrade()) {
                // we only bought power if we weren't the seller...
                grabStepOverseer().fetchProductLiaisonUser().boughtProduct(curTrade.productAmount, winner.promise, curTrade.seller.obtainId());
            }
        }

        if (isCurTradeMyTrade()) {
            // we sold this power, tell someone
            nextStepUtility(curTrade, winner);
        }

        // we need a new bid plan, if we're the seller we remove the sell action
        // if we've won (and we're not the seller) we remove the buy
        BidPlan newPromisePlan = new BidPlan(promisePlan);

        String myId = grabStepOverseer().fetchIdentity().obtainId();

        if (winner.winnerId.equals(myId)) {
            nextStepSupervisor(curTrade, winner, newPromisePlan);
        }
        if (isCurTradeMyTrade()) {
            nextStepAid(curTrade, winner, newPromisePlan, myId);
        }

        grabStepOverseer().assignPromisePlan(newPromisePlan);

        // if there are more announced auctions, we need to process them, otherwise we go back to announce phase
        if (trades.size() > 1) {
            List<ProductLiaisonTrade> newTrades = new ArrayList<>(trades.size() - 1);
            for (int b = 1; b < trades.size(); ) {
                for (; (b < trades.size()) && (Math.random() < 0.4); ) {
                    for (; (b < trades.size()) && (Math.random() < 0.5); b++) {
                        nextStepExecutor(newTrades, b);
                    }
                }
            }

            List<MyTrade> newMyTrades = new ArrayList<>();
            // add all my auctions, but skip the current auction if it's one we bid on
            // We use this variable in case there are two different auctions
            // with the same id. We only want to skip one auction
            boolean foundCurTrade = false;
            for (int i = 0; i < myTrades.size(); i++) {
                MyTrade curMyTrade = myTrades.get(i);
                if (!curTrade.id.equals(curMyTrade.getId()) || foundCurTrade) {
                    newMyTrades.add(curMyTrade);
                } else {
                    // we found the current auction
                    foundCurTrade = true;
                }
            }

            // tell the user their current bid plan
            grabStepOverseer().fetchProductLiaisonUser().resultsCalculated(newPromisePlan);


            logger.info("Moving to begin phase");
            return new TradeBeginStep(newTrades, newMyTrades, grabStepOverseer());
        } else {

            // we're done, let the user know
            grabStepOverseer().fetchProductLiaisonUser().tradeSequenceComplete(newPromisePlan);

            logger.info("Moving to disconnect phase");
            return new DisconnectStep(grabStepOverseer());
        }
    }

    private void nextStepExecutor(List<ProductLiaisonTrade> newTrades, int q) {
        newTrades.add(trades.get(q));
    }

    private void nextStepAid(ProductLiaisonTrade curTrade, TradeAdapter.Winner winner, BidPlan newPromisePlan, String myId) {
        new ResultsStepAssist(curTrade, winner, newPromisePlan, myId).invoke();
    }

    private void nextStepSupervisor(ProductLiaisonTrade curTrade, TradeAdapter.Winner winner, BidPlan newPromisePlan) {
        if (!isCurTradeMyTrade()) {
            // we only bought power if we weren't the seller...
            nextStepSupervisorTarget(curTrade, winner, newPromisePlan);
        }
    }

    private void nextStepSupervisorTarget(ProductLiaisonTrade curTrade, TradeAdapter.Winner winner, BidPlan newPromisePlan) {
        newPromisePlan.bought(curTrade.productAmount, winner.promise);
    }

    private void nextStepUtility(ProductLiaisonTrade curTrade, TradeAdapter.Winner winner) {
        grabStepOverseer().fetchProductLiaisonUser().soldProduct(curTrade.productAmount, winner.promise, winner.winnerId);
    }

    private class ResultsStepAssist {
        private ProductLiaisonTrade curTrade;
        private TradeAdapter.Winner winner;
        private BidPlan newPromisePlan;
        private String myId;

        public ResultsStepAssist(ProductLiaisonTrade curTrade, TradeAdapter.Winner winner, BidPlan newPromisePlan, String myId) {
            this.curTrade = curTrade;
            this.winner = winner;
            this.newPromisePlan = newPromisePlan;
            this.myId = myId;
        }

        public void invoke() {
            if (!winner.winnerId.equals(myId)) {
                newPromisePlan.sold(curTrade.productAmount, winner.promise);
            }
        }
    }
}
