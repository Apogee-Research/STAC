package net.roboticapex.broker;

public class ProductLiaisonDeviation extends Exception {
    public ProductLiaisonDeviation(String message) {
        super(message);
    }

    public ProductLiaisonDeviation(Throwable e) {
        super(e);
    }
}
