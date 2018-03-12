package com.virtualpoint.broker;

public interface ProductIntermediaryUser {
    void barterSequenceComplete(PurchasePlan bidPlan);

    void barterStarted(String id, int productAmount);

    void barterEnded(String id, int productAmount);

    void soldProduct(int productAmount, int bid, String to);

    void boughtProduct(int productAmount, int bid, String from);

    void bidding(int productAmount, int bid);

    void resultsCalculated(PurchasePlan currentBidPlan);

    void disconnectedFromAllUsers();
}
