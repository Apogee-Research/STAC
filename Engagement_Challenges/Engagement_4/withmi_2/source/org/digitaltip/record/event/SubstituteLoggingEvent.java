package org.digitaltip.record.event;

import org.digitaltip.record.Marker;
import org.digitaltip.record.helpers.SubstituteLogger;

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

    public Level obtainLevel() {
        return level;
    }

    public void assignLevel(Level level) {
        this.level = level;
    }

    public Marker getMarker() {
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object[] pullArgumentArray() {
        return argArray;
    }

    public void fixArgumentArray(Object[] argArray) {
        this.argArray = argArray;
    }

    public long pullTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String pullThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public Throwable obtainThrowable() {
        return throwable;
    }

    public void defineThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
