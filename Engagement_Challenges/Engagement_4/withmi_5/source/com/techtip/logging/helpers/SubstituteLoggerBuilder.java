package com.techtip.logging.helpers;

import com.techtip.logging.event.SubstituteLoggingEvent;

import java.util.List;

public class SubstituteLoggerBuilder {
    private String name;
    private List<SubstituteLoggingEvent> eventList;

    public SubstituteLoggerBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public SubstituteLoggerBuilder fixEventList(List<SubstituteLoggingEvent> eventList) {
        this.eventList = eventList;
        return this;
    }

    public SubstituteLogger formSubstituteLogger() {
        return new SubstituteLogger(name, eventList);
    }
}