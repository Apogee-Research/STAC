package com.techtip.logging.event;

import com.techtip.logging.helpers.SubstituteLogger;

import java.util.List;

public class EventRecodingLoggerBuilder {
    private SubstituteLogger logger;
    private List<SubstituteLoggingEvent> eventList;

    public EventRecodingLoggerBuilder fixLogger(SubstituteLogger logger) {
        this.logger = logger;
        return this;
    }

    public EventRecodingLoggerBuilder assignEventList(List<SubstituteLoggingEvent> eventList) {
        this.eventList = eventList;
        return this;
    }

    public EventRecodingLogger formEventRecodingLogger() {
        return new EventRecodingLogger(logger, eventList);
    }
}