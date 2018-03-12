package org.digitalapex.powerbroker.stage;

import org.digitalapex.trade.messagedata.BidConveyData;
import org.digitalapex.talkers.TalkersConnection;
import org.digitalapex.talkers.TalkersRaiser;
import org.digitalapex.talkers.TalkersPublicIdentity;
import org.digitalapex.powerbroker.BidPlan;
import org.digitalapex.powerbroker.MySelloff;
import org.digitalapex.powerbroker.CommodityGoBetweenSelloff;
import org.digitalapex.powerbroker.CommodityGoBetweenRaiser;
import org.digitalapex.powerbroker.Powerbrokermsg;
import org.digitalapex.powerbroker.trade.SelloffAdapter;
import org.digitalapex.logging.Logger;
import org.digitalapex.logging.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class SelloffEndPeriod extends SelloffBasePeriod {
    private final Logger logger = LoggerFactory.obtainLogger(getClass());
    private final SelloffAdapter selloffAdapter;
    private final BidPlan bidPlan;
    private final int myBid;

    /**
     * @param selloffs       the entire list of auctions that we'll be processing at some point.  For the purposes of this
     *                       phase (and most others) we're only every concerned with the first auction on this list. That's
     *                       the 'current' auction.
     * @param mySelloffs     a list of auctions that I've started.
     * @param bidPlan        the bidding plan that indicates what we should try to do.
     * @param selloffAdapter used to help us manage the current auction and the auction library.
     * @param periodOverseer
     */
    public SelloffEndPeriod(List<CommodityGoBetweenSelloff> selloffs, List<MySelloff> mySelloffs, BidPlan bidPlan, SelloffAdapter selloffAdapter, int myBid, PeriodOverseer periodOverseer) {
        super(selloffs, mySelloffs, periodOverseer);
        this.bidPlan = bidPlan;
        this.selloffAdapter = selloffAdapter;
        this.myBid = myBid;
    }

    @Override
    public void enterPeriod() throws CommodityGoBetweenRaiser {
        super.enterPeriod();

        // are we the bidder?  if so, close the auction
        if (isCurSelloffMySelloff()) {
            enterPeriodCoordinator();
        }
    }

    private void enterPeriodCoordinator() throws CommodityGoBetweenRaiser {
        try {
            selloffAdapter.closeSelloff(getCurrentSelloff().id);
        } catch (Exception e) {
            throw new CommodityGoBetweenRaiser(e);
        }
    }

    @Override
    public Period handleMsg(TalkersConnection connection, Powerbrokermsg.BaseMessage msg) throws CommodityGoBetweenRaiser {
        if (msg.getType() == Powerbrokermsg.BaseMessage.Type.AUCTION_DATA) {
            try {
                selloffAdapter.handle(connection.fetchTheirIdentity(), msg.getAuctionData().toByteArray());
            } catch (TalkersRaiser e) {
                throw new CommodityGoBetweenRaiser(e);
            }

            if (isCurSelloffMySelloff()) {
                // we have to wait for all the claims
                if (!selloffAdapter.hasReceivedAllClaimsAndConcessions()) {
                    // we're still expecting more bids, we have to wait
                    return handleMsgUtility();
                } else {

                    // ok, we've got all the claims and concessions, we can announce a winner...
                    Map<TalkersPublicIdentity, BidConveyData> claims = selloffAdapter.pullClaims();
                    logger.info("claims received: " + claims.size());

                    TalkersPublicIdentity winner = null;
                    int winningBid = 0;
                    if (claims.size() == 0) {
                        // I guess I won
                        winner = obtainPeriodOverseer().grabMyPublicIdentity();
                        winningBid = myBid;
                    } else {
                        SortedSet<TalkersPublicIdentity> claimIds = new TreeSet<>(claims.keySet());
                        winner = claimIds.last();
                        winningBid = claims.get(winner).fetchBid();
                    }

                    if (winner == null) {
                        return handleMsgEngine();
                    }

                    try {
                        if (winner.equals(obtainPeriodOverseer().grabMyPublicIdentity())) {
                            logger.info("handleMsg: I won! bid: " + winningBid + " announcing...");
                        } else {
                            logger.info("handleMsg: " + winner.grabTruncatedId() + " won. bid: " + winningBid + " announcing...");
                        }
                        selloffAdapter.announceWinner(getCurrentSelloff().id, winner, winningBid);
                    } catch (Exception e) {
                        throw new CommodityGoBetweenRaiser(e);
                    }

                    // it may be time to start sending these finished messages
                    return handlePotentialEnd();
                }
            } else {
                // we have to wait for the winner announcement
                SelloffAdapter.Winner winner = selloffAdapter.pullWinner();
                if (winner == null) {
                    // we're still expecting more bids, we have to wait
                    logger.info("handleMsg: still waiting for claims and concessions");
                    return null;
                } else {
                    if (winner.winnerId.equals(obtainPeriodOverseer().takeIdentity().pullId())) {
                        logger.info("handleMsg: I won! bid: " + winner.bid);
                    } else {
                        String wid = winner.winnerId;
                        if (wid.length() > 25){
                            wid = wid.substring(0, 25) + "...";
                        }
                        logger.info("handleMsg: " + wid + " won. bid: " + winner.bid);
                    }

                    // it may be time to start sending these finished messages
                    return handlePotentialEnd();
                }
            }
        } else if (msg.getType() == Powerbrokermsg.BaseMessage.Type.AUCTION_END) {
            return handleMsgExecutor(connection, msg);
        } else {
            logger.error("Invalid message type in AuctionEndPhase: " + msg.getType() + " from " +
                    connection.fetchTheirIdentity().grabTruncatedId());
            List<TalkersPublicIdentity> grabFinalMessageFix = obtainPriorPeriod().grabFinalMessageFix();
            for (int c = 0; c < grabFinalMessageFix.size(); c++) {
                TalkersPublicIdentity sender = grabFinalMessageFix.get(c);
                System.out.println("Got final message from: " + sender.grabTruncatedId());
            }
            return null;
        }
    }

    private Period handleMsgExecutor(TalkersConnection connection, Powerbrokermsg.BaseMessage msg) throws CommodityGoBetweenRaiser {
        addFinalMessage(connection.fetchTheirIdentity(), msg);

        return handlePotentialEnd();
    }

    private Period handleMsgEngine() throws CommodityGoBetweenRaiser {
        throw new CommodityGoBetweenRaiser("No winner found!");
    }

    private Period handleMsgUtility() {
        logger.info("handleMsg: still waiting for claims and concessions");
        return null;
    }

    private Period handlePotentialEnd() throws CommodityGoBetweenRaiser {
        if (isItMyTurnToTransmitMessages()) {
            transmitSelloffEndMessage();
        }

        return shouldTransitionToNextPeriod();
    }

    private void transmitSelloffEndMessage() throws CommodityGoBetweenRaiser {
        Powerbrokermsg.BaseMessage baseMessage = Powerbrokermsg.BaseMessage.newBuilder()
                .setType(Powerbrokermsg.BaseMessage.Type.AUCTION_END)
                .build();

        transmitFinalMessage(baseMessage.toByteArray());
    }

    @Override
    protected Period nextPeriod() throws CommodityGoBetweenRaiser {

        // let the user know the auction is over
        obtainPeriodOverseer().grabCommodityGoBetweenUser().selloffEnded(getCurrentSelloff().id, getCurrentSelloff().commodityAmount);

        logger.info("Moving to results phase");

        return new ResultsPeriod(selloffs, mySelloffs, bidPlan, selloffAdapter, obtainPeriodOverseer());
    }
}
