package net.techpoint.graph;

public class SchemeFailure extends Exception {
    public SchemeFailure() {
        super();
    }

    public SchemeFailure(String message) {
        super(message);
    }

    public SchemeFailure(String message, Throwable cause) {
        super(message, cause);
    }

    public SchemeFailure(Throwable cause) {
        super(cause);
    }

    protected SchemeFailure(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
