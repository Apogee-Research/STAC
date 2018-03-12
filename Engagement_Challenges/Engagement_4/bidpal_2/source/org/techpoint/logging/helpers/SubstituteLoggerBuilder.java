package org.techpoint.logging.helpers;

import org.techpoint.logging.event.SubstituteLoggingEvent;

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

    public SubstituteLogger composeSubstituteLogger() {
        return new SubstituteLogger(name, eventList);
    }
}