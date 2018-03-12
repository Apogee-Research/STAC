package org.digitaltip.dialogs;

public class TalkersDeviation extends Exception {
    public TalkersDeviation(String message) {
        super(message);
    }

    public TalkersDeviation(Throwable t) {
        super(t);
    }
}
