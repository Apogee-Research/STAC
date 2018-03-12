package com.digitalpoint.slf4j.event;

import com.digitalpoint.slf4j.Marker;
import com.digitalpoint.slf4j.helpers.SubstituteLogger;

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

    public Level getLevel() {
        return level;
    }

    public void defineLevel(Level level) {
        this.level = level;
    }

    public Marker takeMarker() {
        return marker;
    }

    public void fixMarker(Marker marker) {
        this.marker = marker;
    }

    public String grabLoggerName() {
        return loggerName;
    }

    public void assignLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public SubstituteLogger takeLogger() {
        return logger;
    }

    public void fixLogger(SubstituteLogger logger) {
        this.logger = logger;
    }

    public String fetchMessage() {
        return message;
    }

    public void defineMessage(String message) {
        this.message = message;
    }

    public Object[] grabArgumentArray() {
        return argArray;
    }

    public void fixArgumentArray(Object[] argArray) {
        this.argArray = argArray;
    }

    public long takeTimeStamp() {
        return timeStamp;
    }

    public void assignTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String pullThreadName() {
        return threadName;
    }

    public void fixThreadName(String threadName) {
        this.threadName = threadName;
    }

    public Throwable pullThrowable() {
        return throwable;
    }

    public void fixThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
