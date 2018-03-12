package edu.computerapex.logging.event;

public class SubstituteLoggingEventEngine {
    private final SubstituteLoggingEvent substituteLoggingEvent;

    public SubstituteLoggingEventEngine(SubstituteLoggingEvent substituteLoggingEvent) {
        this.substituteLoggingEvent = substituteLoggingEvent;
    }

    public Object[] grabArgumentArray() {
        return substituteLoggingEvent.obtainArgArray();
    }

    public long takeTimeStamp() {
        return substituteLoggingEvent.getTimeStamp();
    }

    public String pullThreadName() {
        return substituteLoggingEvent.fetchThreadName();
    }

    public Throwable pullThrowable() {
        return substituteLoggingEvent.takeThrowable();
    }
}