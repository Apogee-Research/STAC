package edu.networkcusp.senderReceivers;

public class ProtocolsRaiser extends Exception {
    public ProtocolsRaiser(String message) {
        super(message);
    }

    public ProtocolsRaiser(Throwable t) {
        super(t);
    }
}
