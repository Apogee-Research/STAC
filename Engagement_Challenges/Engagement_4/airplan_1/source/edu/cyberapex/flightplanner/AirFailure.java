package edu.cyberapex.flightplanner;


public class AirFailure extends Exception {

    public AirFailure() {
        super();
    }

    public AirFailure(String message) {
        super(message);
    }

    public AirFailure(String message, Throwable cause) {
        super(message, cause);
    }

    public AirFailure(Throwable cause) {
        super(cause);
    }

    protected AirFailure(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
