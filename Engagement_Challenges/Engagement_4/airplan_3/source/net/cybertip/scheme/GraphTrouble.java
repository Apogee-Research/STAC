package net.cybertip.scheme;

public class GraphTrouble extends Exception {
    public GraphTrouble() {
        super();
    }

    public GraphTrouble(String message) {
        super(message);
    }

    public GraphTrouble(String message, Throwable cause) {
        super(message, cause);
    }

    public GraphTrouble(Throwable cause) {
        super(cause);
    }

    protected GraphTrouble(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
