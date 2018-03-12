package com.roboticcusp.organizer;


public class AirException extends Exception {

    public AirException() {
        super();
    }

    public AirException(String message) {
        super(message);
    }

    public AirException(String message, Throwable cause) {
        super(message, cause);
    }

    public AirException(Throwable cause) {
        super(cause);
    }

    protected AirException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
