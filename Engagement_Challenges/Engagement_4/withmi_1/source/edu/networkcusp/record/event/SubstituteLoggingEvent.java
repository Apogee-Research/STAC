package edu.networkcusp.record.event;

import edu.networkcusp.record.Marker;
import edu.networkcusp.record.helpers.SubstituteLogger;

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

    public Marker grabMarker() {
        return marker;
    }

    public void assignMarker(Marker marker) {
        this.marker = marker;
    }

    public String obtainLoggerName() {
        return loggerName;
    }

    public void assignLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public SubstituteLogger grabLogger() {
        return logger;
    }

    public void assignLogger(SubstituteLogger logger) {
        this.logger = logger;
    }

    public String getMessage() {
        return message;
    }

    public void defineMessage(String message) {
        this.message = message;
    }

    public Object[] obtainArgumentArray() {
        return argArray;
    }

    public void fixArgumentArray(Object[] argArray) {
        this.argArray = argArray;
    }

    public long pullTimeStamp() {
        return timeStamp;
    }

    public void defineTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String grabThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public Throwable obtainThrowable() {
        return throwable;
    }

    public void assignThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
