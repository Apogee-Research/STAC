package com.virtualpoint.logging.implementation;

import org.apache.log4j.Logger;

public class Log4jLoggerAdapterBuilder {
    private Logger logger;

    public Log4jLoggerAdapterBuilder setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    public Log4jLoggerAdapter composeLog4jLoggerAdapter() {
        return new Log4jLoggerAdapter(logger);
    }
}