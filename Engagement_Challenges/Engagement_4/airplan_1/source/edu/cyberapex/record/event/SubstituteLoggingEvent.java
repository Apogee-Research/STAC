package edu.cyberapex.record.event;

import edu.cyberapex.record.Marker;
import edu.cyberapex.record.helpers.SubstituteLogger;

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

    public void setLevel(Level level) {
        this.level = level;
    }

    public Marker grabMarker() {
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

    public SubstituteLogger grabLogger() {
        return logger;
    }

    public void fixLogger(SubstituteLogger logger) {
        this.logger = logger;
    }

    public String obtainMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object[] takeArgumentArray() {
        return argArray;
    }

    public void fixArgumentArray(Object[] argArray) {
        this.argArray = argArray;
    }

    public long grabTimeStamp() {
        return timeStamp;
    }

    public void fixTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String fetchThreadName() {
        return threadName;
    }

    public void defineThreadName(String threadName) {
        this.threadName = threadName;
    }

    public Throwable fetchThrowable() {
        return throwable;
    }

    public void assignThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
