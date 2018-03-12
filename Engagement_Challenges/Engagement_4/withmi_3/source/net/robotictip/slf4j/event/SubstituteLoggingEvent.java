package net.robotictip.slf4j.event;

import net.robotictip.slf4j.Marker;
import net.robotictip.slf4j.helpers.SubstituteLogger;

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

    public Marker getMarker() {
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

    public String obtainMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object[] obtainArgumentArray() {
        return argArray;
    }

    public void fixArgumentArray(Object[] argArray) {
        this.argArray = argArray;
    }

    public long fetchTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String takeThreadName() {
        return threadName;
    }

    public void defineThreadName(String threadName) {
        this.threadName = threadName;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
