package com.virtualpoint.talkers;

public class DialogsTrouble extends Exception {
    public DialogsTrouble(String message) {
        super(message);
    }

    public DialogsTrouble(Throwable t) {
        super(t);
    }
}
