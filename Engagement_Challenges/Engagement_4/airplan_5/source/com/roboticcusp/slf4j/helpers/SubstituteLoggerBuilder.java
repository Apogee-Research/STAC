package com.roboticcusp.slf4j.helpers;

import com.roboticcusp.slf4j.event.SubstituteLoggingEvent;

import java.util.List;

public class SubstituteLoggerBuilder {
    private String name;
    private List<SubstituteLoggingEvent> eventList;

    public SubstituteLoggerBuilder defineName(String name) {
        this.name = name;
        return this;
    }

    public SubstituteLoggerBuilder defineEventList(List<SubstituteLoggingEvent> eventList) {
        this.eventList = eventList;
        return this;
    }

    public SubstituteLogger composeSubstituteLogger() {
        return new SubstituteLogger(name, eventList);
    }
}