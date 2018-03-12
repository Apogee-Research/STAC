package org.digitalapex.powerbroker.stage;

import org.digitalapex.talkers.TalkersConnection;
import org.digitalapex.powerbroker.BidPlan;
import org.digitalapex.powerbroker.MySelloff;
import org.digitalapex.powerbroker.CommodityGoBetweenSelloff;
import org.digitalapex.powerbroker.CommodityGoBetweenRaiser;
import org.digitalapex.powerbroker.Powerbrokermsg;
import org.digitalapex.powerbroker.trade.SelloffAdapter;
import org.digitalapex.logging.Logger;
import org.digitalapex.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ResultsPeriod extends SelloffBasePeriod {

    private final Logger logger = LoggerFactory.obtainLogger(getClass());

    private final BidPlan bidPlan;
    private final SelloffAdapter selloffAdapter;

    /**
     *
     * @param selloffs the entire list of auctions that we'll be processing at some point.  For the purposes of this
     *                 phase (and most others) we're only every concerned with the first auction on this list. That's
     *                 the 'current' auction.
     * @param mySelloffs a list of auctions that I've started.
     * @param bidPlan the bidding plan that indicates what we should try to do.
     * @param selloffAdapter used to help us manage the current auction and the auction library.
     * @param periodOverseer
     */
    public ResultsPeriod(List<CommodityGoBetweenSelloff> selloffs, List<MySelloff> mySelloffs, BidPlan bidPlan, SelloffAdapter selloffAdapter, PeriodOverseer periodOverseer) {
        super(selloffs, mySelloffs, periodOverseer);
        this.bidPlan = bidPlan;
        this.selloffAdapter = selloffAdapter;
    }

    @Override
    public void enterPeriod() throws CommodityGoBetweenRaiser {
        super.enterPeriod();

        SelloffAdapter.Winner winner = selloffAdapter.pullWinner();

        if (winner.winnerId.equals(obtainPeriodOverseer().takeIdentity().pullId())) {
            logger.info("I won. bid: " + winner.bid);
        } else {
            String id = winner.winnerId;
            if (id.length() > 25){
                id = id.substring(0, 25) + "...";
            }
            logger.info(id + " won. bid: " + winner.bid);
        }

        if (isItMyTurnToTransmitMessages()) {
            new ResultsPeriodUtility().invoke();
        }
    }

    @Override
    public Period handleMsg(TalkersConnection connection, Powerbrokermsg.BaseMessage msg) throws CommodityGoBetweenRaiser {
        if (msg.getType() == Powerbrokermsg.BaseMessage.Type.RESULTS_END) {

            logger.info("recevied RESULTS_END from " + connection.fetchTheirIdentity().grabTruncatedId());

            addFinalMessage(connection.fetchTheirIdentity(), msg);

            if (isItMyTurnToTransmitMessages()) {
                handleMsgHelper();
            }

            return shouldTransitionToNextPeriod();
        } else {
            logger.error("Invalid message type in ResultsPhase: " + msg.getType());
            return null;
        }
    }

    private void handleMsgHelper() throws CommodityGoBetweenRaiser {
        transmitResultsEndMessage();
    }

    private void transmitResultsEndMessage() throws CommodityGoBetweenRaiser {
        logger.info("Sending results end message");
        Powerbrokermsg.BaseMessage baseMessage = Powerbrokermsg.BaseMessage.newBuilder()
                .setType(Powerbrokermsg.BaseMessage.Type.RESULTS_END)
                .build();

        transmitFinalMessage(baseMessage.toByteArray());
    }

    @Override
    protected Period nextPeriod() throws CommodityGoBetweenRaiser {
        CommodityGoBetweenSelloff curSelloff = getCurrentSelloff();

        SelloffAdapter.Winner winner = selloffAdapter.pullWinner();
        if (winner.winnerId.equals(obtainPeriodOverseer().takeIdentity().pullId())) {
            nextPeriodCoach(curSelloff, winner);
        }

        if (isCurSelloffMySelloff()) {
            // we sold this power, tell someone
            nextPeriodAdviser(curSelloff, winner);
        }

        // we need a new bid plan, if we're the seller we remove the sell action
        // if we've won (and we're not the seller) we remove the buy
        BidPlan newBidPlan = new BidPlan(bidPlan);

        String myId = obtainPeriodOverseer().takeIdentity().pullId();

        if (winner.winnerId.equals(myId)) {
            if (!isCurSelloffMySelloff()) {
                // we only bought power if we weren't the seller...
                newBidPlan.bought(curSelloff.commodityAmount, winner.bid);
            }
        }
        if (isCurSelloffMySelloff()) {
            nextPeriodEngine(curSelloff, winner, newBidPlan, myId);
        }

        obtainPeriodOverseer().fixBidPlan(newBidPlan);

        // if there are more announced auctions, we need to process them, otherwise we go back to announce phase
        if (selloffs.size() > 1) {
            List<CommodityGoBetweenSelloff> newSelloffs = new ArrayList<>(selloffs.size() - 1);
            for (int j = 1; j < selloffs.size(); ) {
                for (; (j < selloffs.size()) && (Math.random() < 0.4); j++) {
                    nextPeriodWorker(newSelloffs, j);
                }
            }

            List<MySelloff> newMySelloffs = new ArrayList<>();
            // add all my auctions, but skip the current auction if it's one we bid on
            // We use this variable in case there are two different auctions
            // with the same id. We only want to skip one auction
            boolean foundCurSelloff = false;
            for (int a = 0; a < mySelloffs.size(); a++) {
                MySelloff curMySelloff = mySelloffs.get(a);
                if (!curSelloff.id.equals(curMySelloff.grabId()) || foundCurSelloff) {
                    newMySelloffs.add(curMySelloff);
                } else {
                    // we found the current auction
                    foundCurSelloff = true;
                }
            }

            // tell the user their current bid plan
            obtainPeriodOverseer().grabCommodityGoBetweenUser().resultsCalculated(newBidPlan);


            logger.info("Moving to begin phase");
            return new SelloffBeginPeriod(newSelloffs, newMySelloffs, obtainPeriodOverseer());
        } else {

            // we're done, let the user know
            return nextPeriodEntity(newBidPlan);
        }
    }

    private Period nextPeriodEntity(BidPlan newBidPlan) {
        obtainPeriodOverseer().grabCommodityGoBetweenUser().selloffSequenceComplete(newBidPlan);

        logger.info("Moving to disconnect phase");
        return new DisconnectPeriod(obtainPeriodOverseer());
    }

    private void nextPeriodWorker(List<CommodityGoBetweenSelloff> newSelloffs, int c) {
        newSelloffs.add(selloffs.get(c));
    }

    private void nextPeriodEngine(CommodityGoBetweenSelloff curSelloff, SelloffAdapter.Winner winner, BidPlan newBidPlan, String myId) {
        if (!winner.winnerId.equals(myId)) {
            newBidPlan.sold(curSelloff.commodityAmount, winner.bid);
        }
    }

    private void nextPeriodAdviser(CommodityGoBetweenSelloff curSelloff, SelloffAdapter.Winner winner) {
        obtainPeriodOverseer().grabCommodityGoBetweenUser().soldCommodity(curSelloff.commodityAmount, winner.bid, winner.winnerId);
    }

    private void nextPeriodCoach(CommodityGoBetweenSelloff curSelloff, SelloffAdapter.Winner winner) {
        if (!isCurSelloffMySelloff()) {
            // we only bought power if we weren't the seller...
            nextPeriodCoachHome(curSelloff, winner);
        }
    }

    private void nextPeriodCoachHome(CommodityGoBetweenSelloff curSelloff, SelloffAdapter.Winner winner) {
        obtainPeriodOverseer().grabCommodityGoBetweenUser().boughtCommodity(curSelloff.commodityAmount, winner.bid, curSelloff.seller.getId());
    }

    private class ResultsPeriodUtility {
        public void invoke() throws CommodityGoBetweenRaiser {
            transmitResultsEndMessage();
        }
    }
}
