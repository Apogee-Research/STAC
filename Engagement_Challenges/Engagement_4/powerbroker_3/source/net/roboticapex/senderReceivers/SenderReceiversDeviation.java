package net.roboticapex.senderReceivers;

public class SenderReceiversDeviation extends Exception {
    public SenderReceiversDeviation(String message) {
        super(message);
    }

    public SenderReceiversDeviation(Throwable t) {
        super(t);
    }
}
