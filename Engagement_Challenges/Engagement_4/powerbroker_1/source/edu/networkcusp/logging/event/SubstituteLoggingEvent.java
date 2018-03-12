package edu.networkcusp.logging.event;

import edu.networkcusp.logging.Marker;
import edu.networkcusp.logging.helpers.SubstituteLogger;

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

    public Level grabLevel() {
        return level;
    }

    public void assignLevel(Level level) {
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

    public void assignLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public SubstituteLogger takeLogger() {
        return logger;
    }

    public void setLogger(SubstituteLogger logger) {
        this.logger = logger;
    }

    public String takeMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object[] grabArgumentArray() {
        return argArray;
    }

    public void defineArgumentArray(Object[] argArray) {
        this.argArray = argArray;
    }

    public long getTimeStamp() {
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

    public Throwable fetchThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
