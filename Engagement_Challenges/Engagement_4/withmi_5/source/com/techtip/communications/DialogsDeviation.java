package com.techtip.communications;

public class DialogsDeviation extends Exception {
    public DialogsDeviation(String message) {
        super(message);
    }

    public DialogsDeviation(Throwable t) {
        super(t);
    }
}
