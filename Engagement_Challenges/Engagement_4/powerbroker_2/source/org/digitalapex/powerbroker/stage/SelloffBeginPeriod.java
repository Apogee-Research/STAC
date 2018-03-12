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

public class SelloffBeginPeriod extends SelloffBasePeriod {

    private final Logger logger = LoggerFactory.obtainLogger(getClass());
    private final BidPlan bidPlan;
    private final SelloffAdapter selloffAdapter;

    /**
     *
     * @param selloffs the entire list of auctions that we'll be processing at some point.  For the purposes of this
     *                 phase (and most others) we're only ever concerned with the first auction on this list. That's
     *                 the 'current' auction.
     * @param mySelloffs a list of auctions that I've started.
     * @param periodOverseer
     */
    public SelloffBeginPeriod(List<CommodityGoBetweenSelloff> selloffs, List<MySelloff> mySelloffs, PeriodOverseer periodOverseer) {
        super(selloffs, mySelloffs, periodOverseer);
        this.bidPlan = periodOverseer.takeBidPlan();
        this.selloffAdapter = new SelloffAdapter(periodOverseer.takeIdentity(), periodOverseer.pullConnections());
    }

    @Override
    public void enterPeriod() throws CommodityGoBetweenRaiser {
        super.enterPeriod();

        // is this my auction or someone else's?
        // (we're only talking about the current auction)
        if (isItMyTurnToTransmitMessages()) {
            if (isCurSelloffMySelloff()) {
                startSelloff();
            } else {
                transmitSelloffBeginMsg();
            }
        } else {
            enterPeriodService();
        }
    }

    private void enterPeriodService() {
        logger.info("Not my turn to send message");
    }

    @Override
    public Period handleMsg(TalkersConnection connection, Powerbrokermsg.BaseMessage msg) throws CommodityGoBetweenRaiser {

        if (msg.getType() == Powerbrokermsg.BaseMessage.Type.AUCTION_DATA) {
            return new SelloffBeginPeriodAdviser(connection, msg).invoke();
        }

        if (msg.getType() != Powerbrokermsg.BaseMessage.Type.AUCTION_BEGIN) {
            logger.error("Invalid message type in AuctionBeginPhase: " + msg.getType());
            return null;
        }

        logger.info("received begin message from " + connection.fetchTheirIdentity().grabTruncatedId());

        addFinalMessage(connection.fetchTheirIdentity(), msg);

        String expectedId = getCurrentSelloff().id;
        if (!msg.getAuctionBegin().getAuctionId().equals(expectedId)) {
            return new SelloffBeginPeriodAid(msg, expectedId).invoke();
        }

        if (!hasSentFinalMessage) {
            if (isItMyTurnToTransmitMessages()) {
                handleMsgWorker();
            }
        }

        return shouldTransitionToNextPeriod();
    }

    private void handleMsgWorker() throws CommodityGoBetweenRaiser {
        if (isCurSelloffMySelloff()) {
            handleMsgWorkerAid();
        } else {
            handleMsgWorkerHome();
        }
    }

    private void handleMsgWorkerHome() throws CommodityGoBetweenRaiser {
        logger.info("We're observing this auction, we might bid");
        // send auction begin message and move on
        transmitSelloffBeginMsg();
    }

    private void handleMsgWorkerAid() throws CommodityGoBetweenRaiser {
        startSelloff();
    }

    private void startSelloff() throws CommodityGoBetweenRaiser {

        logger.info("Starting auction...");

        CommodityGoBetweenSelloff curSelloff = getCurrentSelloff();
        MySelloff mySelloff = null;

        // find our matching auction
        // makes the assumption that *we* either won't have two auctions
        // for the same amount, or, if we do, that it doesn't
        // matter what order they're resolved in.
        for (int q = 0; q < mySelloffs.size(); q++) {
            MySelloff curMySelloff = mySelloffs.get(q);
            if (curMySelloff.getAmountOfCommodity() == curSelloff.commodityAmount) {
                mySelloff = curMySelloff;
                break;
            }
        }

        if (mySelloff == null) {
            throw new CommodityGoBetweenRaiser("This is our auction but we have no record of it. PowerAmount: " +
                    curSelloff.commodityAmount);
        }

        // we're selling!
        logger.info("We're selling " + curSelloff.commodityAmount + " units of power for at least " +
                mySelloff.fetchReserve());

        try {
            selloffAdapter.startSelloff(mySelloff.grabId(), mySelloff.toString());
        } catch (TalkersRaiser e) {
            throw new CommodityGoBetweenRaiser(e);
        }

        transmitSelloffBeginMsg();
    }

    private void transmitSelloffBeginMsg() throws CommodityGoBetweenRaiser {

        logger.info("Sending auction begin message...");

        // let the user know
        obtainPeriodOverseer().grabCommodityGoBetweenUser().selloffStarted(getCurrentSelloff().id, getCurrentSelloff().commodityAmount);

        CommodityGoBetweenSelloff curSelloff = getCurrentSelloff();

        // We're going to send a message indicating we're going to join in the bidding on this auction.
        // Then we're going to wait for everyone to confirm this is the current auction.
        // (Which they'll do by just repeating the message back to everyone)
        Powerbrokermsg.AuctionBeginMessage beginMessage = Powerbrokermsg.AuctionBeginMessage.newBuilder()
                .setPowerAmount(curSelloff.commodityAmount)
                .setAuctionId(curSelloff.id)
                .build();

        Powerbrokermsg.BaseMessage baseMessage = Powerbrokermsg.BaseMessage.newBuilder()
                .setAuctionBegin(beginMessage)
                .setType(Powerbrokermsg.BaseMessage.Type.AUCTION_BEGIN)
                .build();

        transmitFinalMessage(baseMessage.toByteArray());
    }

    protected Period nextPeriod() throws CommodityGoBetweenRaiser {
        logger.info("Moving to bidding phase");
        return new BiddingPeriod(selloffs, mySelloffs, selloffAdapter, obtainPeriodOverseer());
    }

    private class SelloffBeginPeriodAdviser {
        private TalkersConnection connection;
        private Powerbrokermsg.BaseMessage msg;

        public SelloffBeginPeriodAdviser(TalkersConnection connection, Powerbrokermsg.BaseMessage msg) {
            this.connection = connection;
            this.msg = msg;
        }

        public Period invoke() throws CommodityGoBetweenRaiser {
            try {
                selloffAdapter.handle(connection.fetchTheirIdentity(), msg.getAuctionData().toByteArray());
            } catch (TalkersRaiser e) {
                throw new CommodityGoBetweenRaiser(e);
            }

            // we don't need to do any further processing...
            return null;
        }
    }

    private class SelloffBeginPeriodAid {
        private Powerbrokermsg.BaseMessage msg;
        private String expectedId;

        public SelloffBeginPeriodAid(Powerbrokermsg.BaseMessage msg, String expectedId) {
            this.msg = msg;
            this.expectedId = expectedId;
        }

        public Period invoke() throws CommodityGoBetweenRaiser {
            throw new CommodityGoBetweenRaiser("Unexpected auction id. Expected: [" + expectedId + "] got ["
                    + msg.getAuctionBegin().getAuctionId());
        }
    }
}
