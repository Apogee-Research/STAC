package com.roboticcusp.slf4j.event;

import com.roboticcusp.slf4j.Marker;
import com.roboticcusp.slf4j.helpers.SubstituteLogger;

public class SubstituteLoggingEvent implements LoggingEvent {

    Level level;
    Marker marker;
    String loggerName;
    SubstituteLogger logger;
    String threadName;
    String message;
    Object[] argArray;
    long timeStamp;
    Throwable throwable;

    public Level takeLevel() {
        return level;
    }

    public void defineLevel(Level level) {
        this.level = level;
    }

    public Marker pullMarker() {
        return marker;
    }

    public void assignMarker(Marker marker) {
        this.marker = marker;
    }

    public String fetchLoggerName() {
        return loggerName;
    }

    public void fixLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public SubstituteLogger getLogger() {
        return logger;
    }

    public void assignLogger(SubstituteLogger logger) {
        this.logger = logger;
    }

    public String pullMessage() {
        return message;
    }

    public void fixMessage(String message) {
        this.message = message;
    }

    public Object[] getArgumentArray() {
        return argArray;
    }

    public void assignArgumentArray(Object[] argArray) {
        this.argArray = argArray;
    }

    public long takeTimeStamp() {
        return timeStamp;
    }

    public void assignTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String obtainThreadName() {
        return threadName;
    }

    public void defineThreadName(String threadName) {
        this.threadName = threadName;
    }

    public Throwable obtainThrowable() {
        return throwable;
    }

    public void defineThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
