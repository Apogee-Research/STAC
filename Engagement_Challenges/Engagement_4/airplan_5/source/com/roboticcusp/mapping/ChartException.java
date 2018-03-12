package com.roboticcusp.mapping;

public class ChartException extends Exception {
    public ChartException() {
        super();
    }

    public ChartException(String message) {
        super(message);
    }

    public ChartException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChartException(Throwable cause) {
        super(cause);
    }

    protected ChartException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
