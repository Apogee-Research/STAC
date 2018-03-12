package net.roboticapex.broker;

public interface ProductLiaisonUser {
    void tradeSequenceComplete(BidPlan promisePlan);

    void tradeStarted(String id, int productAmount);

    void tradeEnded(String id, int productAmount);

    void soldProduct(int productAmount, int promise, String to);

    void boughtProduct(int productAmount, int promise, String from);

    void bidding(int productAmount, int promise);

    void resultsCalculated(BidPlan currentPromisePlan);

    void disconnectedFromAllUsers();
}
