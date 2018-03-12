package com.networkapex.chart;

public class GraphRaiser extends Exception {
    public GraphRaiser() {
        super();
    }

    public GraphRaiser(String message) {
        super(message);
    }

    public GraphRaiser(String message, Throwable cause) {
        super(message, cause);
    }

    public GraphRaiser(Throwable cause) {
        super(cause);
    }

    protected GraphRaiser(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
