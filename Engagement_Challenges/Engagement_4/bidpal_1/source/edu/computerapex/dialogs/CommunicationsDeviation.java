package edu.computerapex.dialogs;

public class CommunicationsDeviation extends Exception {
    public CommunicationsDeviation(String message) {
        super(message);
    }

    public CommunicationsDeviation(Throwable t) {
        super(t);
    }
}
