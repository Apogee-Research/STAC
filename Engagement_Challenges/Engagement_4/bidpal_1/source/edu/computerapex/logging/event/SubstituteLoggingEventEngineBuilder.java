package edu.computerapex.logging.event;

public class SubstituteLoggingEventEngineBuilder {
    private SubstituteLoggingEvent substituteLoggingEvent;

    public SubstituteLoggingEventEngineBuilder defineSubstituteLoggingEvent(SubstituteLoggingEvent substituteLoggingEvent) {
        this.substituteLoggingEvent = substituteLoggingEvent;
        return this;
    }

    public SubstituteLoggingEventEngine generateSubstituteLoggingEventEngine() {
        return new SubstituteLoggingEventEngine(substituteLoggingEvent);
    }
}