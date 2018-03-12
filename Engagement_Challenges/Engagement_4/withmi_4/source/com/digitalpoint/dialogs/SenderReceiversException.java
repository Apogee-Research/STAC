package com.digitalpoint.dialogs;

public class SenderReceiversException extends Exception {
    public SenderReceiversException(String message) {
        super(message);
    }

    public SenderReceiversException(Throwable t) {
        super(t);
    }
}
