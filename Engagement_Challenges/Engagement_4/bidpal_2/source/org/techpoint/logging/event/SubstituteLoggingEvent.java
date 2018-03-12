package org.techpoint.logging.event;

import org.techpoint.logging.Marker;
import org.techpoint.logging.helpers.SubstituteLogger;

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

    public void assignLevel(Level level) {
        this.level = level;
    }

    public Marker fetchMarker() {
        return marker;
    }

    public void fixMarker(Marker marker) {
        this.marker = marker;
    }

    public String grabLoggerName() {
        return loggerName;
    }

    public void fixLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public SubstituteLogger obtainLogger() {
        return logger;
    }

    public void assignLogger(SubstituteLogger logger) {
        this.logger = logger;
    }

    public String obtainMessage() {
        return message;
    }

    public void fixMessage(String message) {
        this.message = message;
    }

    public Object[] grabArgumentArray() {
        return argArray;
    }

    public void defineArgumentArray(Object[] argArray) {
        this.argArray = argArray;
    }

    public long pullTimeStamp() {
        return timeStamp;
    }

    public void fixTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String pullThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
