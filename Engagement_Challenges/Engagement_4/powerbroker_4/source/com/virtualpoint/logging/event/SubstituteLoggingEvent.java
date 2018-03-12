package com.virtualpoint.logging.event;

import com.virtualpoint.logging.Marker;
import com.virtualpoint.logging.helpers.SubstituteLogger;

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

    public Level fetchLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public Marker pullMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public String obtainLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public SubstituteLogger obtainLogger() {
        return logger;
    }

    public void fixLogger(SubstituteLogger logger) {
        this.logger = logger;
    }

    public String pullMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object[] obtainArgumentArray() {
        return argArray;
    }

    public void setArgumentArray(Object[] argArray) {
        this.argArray = argArray;
    }

    public long fetchTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String pullThreadName() {
        return threadName;
    }

    public void assignThreadName(String threadName) {
        this.threadName = threadName;
    }

    public Throwable grabThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
