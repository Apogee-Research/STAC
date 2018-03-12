package net.computerpoint.logging.event;

import net.computerpoint.logging.Marker;
import net.computerpoint.logging.helpers.SubstituteLogger;

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

    public void defineLevel(Level level) {
        this.level = level;
    }

    public Marker pullMarker() {
        return marker;
    }

    public void assignMarker(Marker marker) {
        this.marker = marker;
    }

    public String obtainLoggerName() {
        return loggerName;
    }

    public void fixLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public SubstituteLogger obtainLogger() {
        return logger;
    }

    public void setLogger(SubstituteLogger logger) {
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

    public void assignArgumentArray(Object[] argArray) {
        this.argArray = argArray;
    }

    public long grabTimeStamp() {
        return timeStamp;
    }

    public void defineTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String grabThreadName() {
        return threadName;
    }

    public void fixThreadName(String threadName) {
        this.threadName = threadName;
    }

    public Throwable grabThrowable() {
        return throwable;
    }

    public void defineThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
