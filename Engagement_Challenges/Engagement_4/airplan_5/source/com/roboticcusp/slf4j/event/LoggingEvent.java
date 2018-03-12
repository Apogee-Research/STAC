package com.roboticcusp.slf4j.event;

import com.roboticcusp.slf4j.Marker;

/**
 * 
 * @author ceki
 * @since 1.7.15
 */
public interface LoggingEvent {

    Level takeLevel();

    Marker pullMarker();

    String fetchLoggerName();

    String pullMessage();

    String obtainThreadName();

    Object[] getArgumentArray();

    long takeTimeStamp();

    Throwable obtainThrowable();

}
