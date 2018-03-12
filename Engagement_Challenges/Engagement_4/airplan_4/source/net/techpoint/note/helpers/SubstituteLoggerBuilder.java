package net.techpoint.note.helpers;

import net.techpoint.note.event.SubstituteLoggingEvent;

import java.util.List;

public class SubstituteLoggerBuilder {
    private String name;
    private List<SubstituteLoggingEvent> eventList;

    public SubstituteLoggerBuilder fixName(String name) {
        this.name = name;
        return this;
    }

    public SubstituteLoggerBuilder setEventList(List<SubstituteLoggingEvent> eventList) {
        this.eventList = eventList;
        return this;
    }

    public SubstituteLogger formSubstituteLogger() {
        return new SubstituteLogger(name, eventList);
    }
}