package net.roboticapex.logging.event;

import net.roboticapex.logging.Marker;
import net.roboticapex.logging.helpers.SubstituteLogger;

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

    public Marker obtainMarker() {
        return marker;
    }

    public void defineMarker(Marker marker) {
        this.marker = marker;
    }

    public String grabLoggerName() {
        return loggerName;
    }

    public void assignLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public SubstituteLogger grabLogger() {
        return logger;
    }

    public void defineLogger(SubstituteLogger logger) {
        this.logger = logger;
    }

    public String takeMessage() {
        return message;
    }

    public void defineMessage(String message) {
        this.message = message;
    }

    public Object[] fetchArgumentArray() {
        return argArray;
    }

    public void defineArgumentArray(Object[] argArray) {
        this.argArray = argArray;
    }

    public long pullTimeStamp() {
        return timeStamp;
    }

    public void defineTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String fetchThreadName() {
        return threadName;
    }

    public void defineThreadName(String threadName) {
        this.threadName = threadName;
    }

    public Throwable takeThrowable() {
        return throwable;
    }

    public void fixThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
