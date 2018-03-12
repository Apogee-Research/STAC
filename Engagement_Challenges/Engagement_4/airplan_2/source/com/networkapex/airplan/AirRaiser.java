package com.networkapex.airplan;


public class AirRaiser extends Exception {

    public AirRaiser() {
        super();
    }

    public AirRaiser(String message) {
        super(message);
    }

    public AirRaiser(String message, Throwable cause) {
        super(message, cause);
    }

    public AirRaiser(Throwable cause) {
        super(cause);
    }

    protected AirRaiser(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
