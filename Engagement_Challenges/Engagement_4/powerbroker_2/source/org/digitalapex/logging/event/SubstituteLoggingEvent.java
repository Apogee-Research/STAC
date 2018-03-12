package org.digitalapex.logging.event;

import org.digitalapex.logging.Marker;
import org.digitalapex.logging.helpers.SubstituteLogger;

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

    public void fixLevel(Level level) {
        this.level = level;
    }

    public Marker obtainMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public String obtainLoggerName() {
        return loggerName;
    }

    public void assignLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public SubstituteLogger fetchLogger() {
        return logger;
    }

    public void setLogger(SubstituteLogger logger) {
        this.logger = logger;
    }

    public String fetchMessage() {
        return message;
    }

    public void assignMessage(String message) {
        this.message = message;
    }

    public Object[] grabArgumentArray() {
        return argArray;
    }

    public void assignArgumentArray(Object[] argArray) {
        this.argArray = argArray;
    }

    public long obtainTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String obtainThreadName() {
        return threadName;
    }

    public void fixThreadName(String threadName) {
        this.threadName = threadName;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void defineThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
