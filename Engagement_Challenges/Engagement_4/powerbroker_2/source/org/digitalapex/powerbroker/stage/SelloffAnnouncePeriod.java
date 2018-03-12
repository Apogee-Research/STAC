package org.digitalapex.powerbroker.stage;

import org.digitalapex.talkers.TalkersConnection;
import org.digitalapex.powerbroker.BidPlan;
import org.digitalapex.powerbroker.MySelloff;
import org.digitalapex.powerbroker.CommodityGoBetweenSelloff;
import org.digitalapex.powerbroker.CommodityGoBetweenRaiser;
import org.digitalapex.powerbroker.Powerbrokermsg;
import org.digitalapex.logging.Logger;
import org.digitalapex.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SelloffAnnouncePeriod extends Period {
    private static final Logger logger = LoggerFactory.obtainLogger(SelloffAnnouncePeriod.class);
    private static final int MAX_NUM_AUCTIONS = 5; // small limit just because they take so long
    private static int numSelloffsSoFar = 0;
    private final List<CommodityGoBetweenSelloff> allSelloffs;
    private final BidPlan bidPlan;
    private List<MySelloff> mySelloffs = null;

    public SelloffAnnouncePeriod(PeriodOverseer periodOverseer) {
        super(periodOverseer);
        bidPlan = periodOverseer.takeBidPlan();
        allSelloffs = new ArrayList<>();
    }

    @Override
    public void enterPeriod() throws CommodityGoBetweenRaiser {
        super.enterPeriod();

        // first create the auctions specified in the bid plan
        mySelloffs = new LinkedList<>();
        List<BidPlan.SellAction> takeSellActions = bidPlan.takeSellActions();
        for (int a = 0; a < takeSellActions.size(); a++) {
            BidPlan.SellAction action = takeSellActions.get(a);
            mySelloffs.add(generateMySelloff(action.commodityAmount, action.price));
        }

        // announce those auctions to the other powerbroker instances
        announceSelloffs();
    }

    private MySelloff generateMySelloff(int commodityAmount, int price) {
        String id = obtainPeriodOverseer().takeIdentity().pullId() + ":" + commodityAmount;
        return new MySelloff(id, commodityAmount, price);
    }

    @Override
    public Period handleMsg(TalkersConnection connection, Powerbrokermsg.BaseMessage msg) throws CommodityGoBetweenRaiser {
        if (msg.getType() != Powerbrokermsg.BaseMessage.Type.AUCTION_ANNOUNCE) {
            logger.error("Invalid message type in AuctionAnnouncePhase: " + msg.getType());
            return null;
        }

        logger.info("received announce message from " + connection.fetchTheirIdentity().grabTruncatedId());

        addFinalMessage(connection.fetchTheirIdentity(), msg);

        List<Powerbrokermsg.AuctionAnnounceMessage> selloffAnnounceList = msg.getAuctionAnnounceList();
        for (int p = 0; p < selloffAnnounceList.size(); p++) {
            handleMsgHerder(connection, selloffAnnounceList, p);
        }

        // we have to wait our turn to send a message...
        if (isItMyTurnToTransmitMessages()) {
            handleMsgFunction();
        }

        return shouldTransitionToNextPeriod();
    }

    private void handleMsgFunction() throws CommodityGoBetweenRaiser {
        transmitAnnounceMessages();
    }

    private void handleMsgHerder(TalkersConnection connection, List<Powerbrokermsg.AuctionAnnounceMessage> selloffAnnounceList, int a) {
        Powerbrokermsg.AuctionAnnounceMessage announceMessage = selloffAnnounceList.get(a);
        logger.info("Got announcement from " + connection.fetchTheirIdentity().getId() + " for power: " +
                announceMessage.getPowerAmount());
        CommodityGoBetweenSelloff selloff = new CommodityGoBetweenSelloff(announceMessage.getId(), connection.fetchTheirIdentity(),
                announceMessage.getPowerAmount());
        allSelloffs.add(selloff);
        numSelloffsSoFar++;
    }

    public Period announceSelloffs() throws CommodityGoBetweenRaiser {
        // we have to wait our turn to send a message...
        if (isItMyTurnToTransmitMessages()) {
            transmitAnnounceMessages();
        }

        return shouldTransitionToNextPeriod();
    }

    /**
     * @return the next phase to transition to
     */
    @Override
    protected Period nextPeriod() throws CommodityGoBetweenRaiser {
        if (allSelloffs.isEmpty()) {
            // No Auctions exist, so we're done; let the user know
            return nextPeriodGateKeeper();
        } else {
            // we need to to provide the auctions in the order they'll be worked
            Collections.sort(allSelloffs);
            // we want them largest to smallest, prior sort gives them smallest to largest
            Collections.reverse(allSelloffs);
            logger.info("Moving to auction begin phase");
            return new SelloffBeginPeriod(allSelloffs, mySelloffs, obtainPeriodOverseer());
        }
    }

    private Period nextPeriodGateKeeper() {
        obtainPeriodOverseer().grabCommodityGoBetweenUser().selloffSequenceComplete(bidPlan);

        logger.info("Moving to disconnect phase");
        return new DisconnectPeriod(obtainPeriodOverseer());
    }

    private void transmitAnnounceMessages() throws CommodityGoBetweenRaiser {
        if (mySelloffs == null) {
            // can't send messages yet
            return;
        }

        // send out announce message for each auction
        Powerbrokermsg.BaseMessage.Builder baseBuilder = Powerbrokermsg.BaseMessage.newBuilder();
        baseBuilder.setType(Powerbrokermsg.BaseMessage.Type.AUCTION_ANNOUNCE);

        int numMySelloffsAnnounced = 0;
        for (int a = 0; a < mySelloffs.size(); a++) {
            MySelloff selloff = mySelloffs.get(a);
            if (numSelloffsSoFar < MAX_NUM_AUCTIONS) {
                CommodityGoBetweenSelloff pbSelloff = new CommodityGoBetweenSelloff(selloff.grabId(),
                        obtainPeriodOverseer().grabMyPublicIdentity(),
                        selloff.getAmountOfCommodity());
                allSelloffs.add(pbSelloff);
                numSelloffsSoFar++;
                numMySelloffsAnnounced++;

                // send the auction announcement
                Powerbrokermsg.AuctionAnnounceMessage.Builder announceMessageBuilder =
                        Powerbrokermsg.AuctionAnnounceMessage.newBuilder()
                                .setPowerAmount(selloff.getAmountOfCommodity())
                                .setId(selloff.grabId());

                baseBuilder.addAuctionAnnounce(announceMessageBuilder).build();
            } else {
                System.err.println("Too many auctions required.  Not going to announce any more.");
                mySelloffs = mySelloffs.subList(0, numMySelloffsAnnounced);
                break;
            }
        }

        transmitFinalMessage(baseBuilder.build().toByteArray());
    }
}
