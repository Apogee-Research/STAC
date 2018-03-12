package org.digitalapex.powerbroker;

public interface CommodityGoBetweenUser {
    void selloffSequenceComplete(BidPlan bidPlan);

    void selloffStarted(String id, int commodityAmount);

    void selloffEnded(String id, int commodityAmount);

    void soldCommodity(int commodityAmount, int bid, String to);

    void boughtCommodity(int commodityAmount, int bid, String from);

    void bidding(int commodityAmount, int bid);

    void resultsCalculated(BidPlan currentBidPlan);

    void disconnectedFromAllUsers();
}
