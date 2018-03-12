package com.techtip.logging.event;

import com.techtip.logging.Marker;
import com.techtip.logging.helpers.SubstituteLogger;

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

    public Level pullLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public Marker takeMarker() {
        return marker;
    }

    public void fixMarker(Marker marker) {
        this.marker = marker;
    }

    public String obtainLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public SubstituteLogger fetchLogger() {
        return logger;
    }

    public void defineLogger(SubstituteLogger logger) {
        this.logger = logger;
    }

    public String takeMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object[] getArgumentArray() {
        return argArray;
    }

    public void fixArgumentArray(Object[] argArray) {
        this.argArray = argArray;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void fixTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String takeThreadName() {
        return threadName;
    }

    public void fixThreadName(String threadName) {
        this.threadName = threadName;
    }

    public Throwable fetchThrowable() {
        return throwable;
    }

    public void fixThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
