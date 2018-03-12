package net.robotictip.protocols;

public class SenderReceiversTrouble extends Exception {
    public SenderReceiversTrouble(String message) {
        super(message);
    }

    public SenderReceiversTrouble(Throwable t) {
        super(t);
    }
}
