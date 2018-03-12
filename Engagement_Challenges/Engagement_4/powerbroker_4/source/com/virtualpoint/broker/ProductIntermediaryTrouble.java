package com.virtualpoint.broker;

public class ProductIntermediaryTrouble extends Exception {
    public ProductIntermediaryTrouble(String message) {
        super(message);
    }

    public ProductIntermediaryTrouble(Throwable e) {
        super(e);
    }
}
