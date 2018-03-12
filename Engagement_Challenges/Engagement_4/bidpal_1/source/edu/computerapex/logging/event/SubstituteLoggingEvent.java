package edu.computerapex.logging.event;

import edu.computerapex.logging.Marker;
import edu.computerapex.logging.helpers.SubstituteLogger;

public class SubstituteLoggingEvent implements LoggingEvent {

    private final SubstituteLoggingEventEngine substituteLoggingEventEngine = new SubstituteLoggingEventEngineBuilder().defineSubstituteLoggingEvent(this).generateSubstituteLoggingEventEngine();
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

    public void defineLevel(Level level) {
        this.level = level;
    }

    public Marker fetchMarker() {
        return marker;
    }

    public void fixMarker(Marker marker) {
        this.marker = marker;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public SubstituteLogger pullLogger() {
        return logger;
    }

    public void assignLogger(SubstituteLogger logger) {
        this.logger = logger;
    }

    public String fetchMessage() {
        return message;
    }

    public void defineMessage(String message) {
        this.message = message;
    }

    public Object[] grabArgumentArray() {
        return substituteLoggingEventEngine.grabArgumentArray();
    }

    public void assignArgumentArray(Object[] argArray) {
        this.argArray = argArray;
    }

    public long takeTimeStamp() {
        return substituteLoggingEventEngine.takeTimeStamp();
    }

    public void fixTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String pullThreadName() {
        return substituteLoggingEventEngine.pullThreadName();
    }

    public void defineThreadName(String threadName) {
        this.threadName = threadName;
    }

    public Throwable pullThrowable() {
        return substituteLoggingEventEngine.pullThrowable();
    }

    public void assignThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public Object[] obtainArgArray() {
        return argArray;
    }

    public String fetchThreadName() {
        return threadName;
    }

    public Throwable takeThrowable() {
        return throwable;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
