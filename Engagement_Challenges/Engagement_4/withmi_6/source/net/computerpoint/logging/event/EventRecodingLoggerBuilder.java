package net.computerpoint.logging.event;

import net.computerpoint.logging.helpers.SubstituteLogger;

import java.util.List;

public class EventRecodingLoggerBuilder {
    private SubstituteLogger logger;
    private List<SubstituteLoggingEvent> eventList;

    public EventRecodingLoggerBuilder assignLogger(SubstituteLogger logger) {
        this.logger = logger;
        return this;
    }

    public EventRecodingLoggerBuilder fixEventList(List<SubstituteLoggingEvent> eventList) {
        this.eventList = eventList;
        return this;
    }

    public EventRecodingLogger formEventRecodingLogger() {
        return new EventRecodingLogger(logger, eventList);
    }
}