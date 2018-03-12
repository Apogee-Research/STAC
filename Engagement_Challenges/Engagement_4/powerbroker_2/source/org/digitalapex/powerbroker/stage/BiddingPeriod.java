package org.digitalapex.powerbroker.stage;

import org.digitalapex.talkers.TalkersConnection;
import org.digitalapex.talkers.TalkersRaiser;
import org.digitalapex.powerbroker.BidPlan;
import org.digitalapex.powerbroker.MySelloff;
import org.digitalapex.powerbroker.CommodityGoBetweenSelloff;
import org.digitalapex.powerbroker.CommodityGoBetweenRaiser;
import org.digitalapex.powerbroker.Powerbrokermsg;
import org.digitalapex.powerbroker.trade.SelloffAdapter;
import org.digitalapex.logging.Logger;
import org.digitalapex.logging.LoggerFactory;

import java.util.List;

public class BiddingPeriod extends SelloffBasePeriod {
    private final Logger logger = LoggerFactory.obtainLogger(getClass());

    private final BidPlan bidPlan;
    private final SelloffAdapter selloffAdapter;
    private int myBid = 0;

    /**
     *
     * @param selloffs the entire list of auctions that we'll be processing at some point.  For the purposes of this
     *                 phase (and most others) we're only every concerned with the first auction on this list. That's
     *                 the 'current' auction.
     * @param mySelloffs a list of auctions that I've started.
     * @param selloffAdapter used to help us manage the current auction and the auction library.
     * @param periodOverseer
     */
    public BiddingPeriod(List<CommodityGoBetweenSelloff> selloffs, List<MySelloff> mySelloffs, SelloffAdapter selloffAdapter, PeriodOverseer periodOverseer) {
        super(selloffs, mySelloffs, periodOverseer);
        this.bidPlan = periodOverseer.takeBidPlan();
        this.selloffAdapter = selloffAdapter;
    }

    @Override
    public void enterPeriod() throws CommodityGoBetweenRaiser {
        super.enterPeriod();

        // are we bidding on this auction? if so, bid
        CommodityGoBetweenSelloff currentSelloff = getCurrentSelloff();

        try {
            if (isCurSelloffMySelloff()) {
                boolean bidOnOurSelloff = false;

                // find this in our auctions...
                for (int c = 0; c < mySelloffs.size(); c++) {
                    MySelloff mySelloff = mySelloffs.get(c);

                    if (mySelloff.grabId().equals(currentSelloff.id)) {


                        logger.info("Going to make a reserve bid of " + mySelloff.fetchReserve());
                        myBid = mySelloff.fetchReserve();
                        bidOnOurSelloff = true;
                    }
                    logger.info("have auction " + mySelloff.grabId() + " reserve " + mySelloff.fetchReserve());
                }

                if (!bidOnOurSelloff) {
                    enterPeriodHelper(currentSelloff);
                }

            } else {
                // we always bid, even if it's 0
                myBid = bidPlan.calcAmountToBid(currentSelloff);
            }

            logger.info("Bidding " + myBid + " for " + currentSelloff.commodityAmount + " units ");
            obtainPeriodOverseer().grabCommodityGoBetweenUser().bidding(currentSelloff.commodityAmount, myBid);
            selloffAdapter.bid(currentSelloff.id, myBid);
        } catch (Exception e) {
            throw new CommodityGoBetweenRaiser(e);
        }
    }

    private void enterPeriodHelper(CommodityGoBetweenSelloff currentSelloff) throws CommodityGoBetweenRaiser {
        new BiddingPeriodHelp(currentSelloff).invoke();
    }

    @Override
    public Period handleMsg(TalkersConnection connection, Powerbrokermsg.BaseMessage msg) throws CommodityGoBetweenRaiser {

        if (msg.getType() == Powerbrokermsg.BaseMessage.Type.AUCTION_DATA) {
            try {
                selloffAdapter.handle(connection.fetchTheirIdentity(), msg.getAuctionData().toByteArray());
            } catch (TalkersRaiser e) {
                throw new CommodityGoBetweenRaiser(e);
            }

            if (!selloffAdapter.hasReceivedAllExpectedBids()) {
                // we're still expecting more bids, we have to wait
                logger.info("handleMsg: still waiting for bids");
                return null;
            } else {

                // it may be time to start sending these finished messages
                if (isItMyTurnToTransmitMessages()) {
                    transmitBiddingFinishedMessage();
                }
                return shouldTransitionToNextPeriod();
            }
        } else if (msg.getType() == Powerbrokermsg.BaseMessage.Type.BIDDING_FINISHED) {
            addFinalMessage(connection.fetchTheirIdentity(), msg);

            if (isItMyTurnToTransmitMessages()) {
                transmitBiddingFinishedMessage();
            }

            return shouldTransitionToNextPeriod();
        } else {
            logger.error("Invalid message type in BiddingPhase: " + msg.getType());
            return null;
        }
    }

    @Override
    protected Period nextPeriod() throws CommodityGoBetweenRaiser {
        return new SelloffEndPeriod(selloffs, mySelloffs, bidPlan, selloffAdapter, myBid, obtainPeriodOverseer());
    }

    private void transmitBiddingFinishedMessage() throws CommodityGoBetweenRaiser {
        Powerbrokermsg.BaseMessage baseMessage = Powerbrokermsg.BaseMessage.newBuilder()
                .setType(Powerbrokermsg.BaseMessage.Type.BIDDING_FINISHED)
                .build();

        transmitFinalMessage(baseMessage.toByteArray());
    }

    private class BiddingPeriodHelp {
        private CommodityGoBetweenSelloff currentSelloff;

        public BiddingPeriodHelp(CommodityGoBetweenSelloff currentSelloff) {
            this.currentSelloff = currentSelloff;
        }

        public void invoke() throws CommodityGoBetweenRaiser {
            throw new CommodityGoBetweenRaiser("This is our auction, but we couldn't " +
                    "find the MyAuction object associated with it " + currentSelloff.id);
        }
    }
}
