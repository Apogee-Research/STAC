package net.techpoint.note.event;

import net.techpoint.note.Marker;
import net.techpoint.note.helpers.SubstituteLogger;

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

    public Marker takeMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void fixLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public SubstituteLogger takeLogger() {
        return logger;
    }

    public void fixLogger(SubstituteLogger logger) {
        this.logger = logger;
    }

    public String pullMessage() {
        return message;
    }

    public void assignMessage(String message) {
        this.message = message;
    }

    public Object[] grabArgumentArray() {
        return argArray;
    }

    public void fixArgumentArray(Object[] argArray) {
        this.argArray = argArray;
    }

    public long obtainTimeStamp() {
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

    public Throwable pullThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
