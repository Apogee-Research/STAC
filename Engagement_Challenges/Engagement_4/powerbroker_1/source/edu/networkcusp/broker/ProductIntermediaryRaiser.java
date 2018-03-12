package edu.networkcusp.broker;

public class ProductIntermediaryRaiser extends Exception {
    public ProductIntermediaryRaiser(String message) {
        super(message);
    }

    public ProductIntermediaryRaiser(Throwable e) {
        super(e);
    }
}
