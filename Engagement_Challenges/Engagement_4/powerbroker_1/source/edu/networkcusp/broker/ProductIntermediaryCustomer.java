package edu.networkcusp.broker;

public interface ProductIntermediaryCustomer {
    void auctionSequenceComplete(PurchasePlan offerPlan);

    void auctionStarted(String id, int productAmount);

    void auctionEnded(String id, int productAmount);

    void soldProduct(int productAmount, int offer, String to);

    void boughtProduct(int productAmount, int offer, String from);

    void bidding(int productAmount, int offer);

    void resultsCalculated(PurchasePlan currentOfferPlan);

    void disconnectedFromAllCustomers();
}
