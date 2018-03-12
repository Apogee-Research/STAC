package org.techpoint.communications;

public class CommsRaiser extends Exception {
    public CommsRaiser(String message) {
        super(message);
    }

    public CommsRaiser(Throwable t) {
        super(t);
    }
}
