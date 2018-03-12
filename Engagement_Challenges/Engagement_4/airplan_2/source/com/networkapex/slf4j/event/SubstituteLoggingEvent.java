package com.networkapex.slf4j.event;

import com.networkapex.slf4j.Marker;
import com.networkapex.slf4j.helpers.SubstituteLogger;

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

    public Level grabLevel() {
        return level;
    }

    public void assignLevel(Level level) {
        this.level = level;
    }

    public Marker takeMarker() {
        return marker;
    }

    public void fixMarker(Marker marker) {
        this.marker = marker;
    }

    public String fetchLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public SubstituteLogger obtainLogger() {
        return logger;
    }

    public void setLogger(SubstituteLogger logger) {
        this.logger = logger;
    }

    public String takeMessage() {
        return message;
    }

    public void fixMessage(String message) {
        this.message = message;
    }

    public Object[] takeArgumentArray() {
        return argArray;
    }

    public void fixArgumentArray(Object[] argArray) {
        this.argArray = argArray;
    }

    public long pullTimeStamp() {
        return timeStamp;
    }

    public void fixTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String obtainThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public Throwable takeThrowable() {
        return throwable;
    }

    public void assignThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
