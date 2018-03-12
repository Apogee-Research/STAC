package com.networkapex.slf4j.event;

import com.networkapex.slf4j.Marker;

/**
 * 
 * @author ceki
 * @since 1.7.15
 */
public interface LoggingEvent {

    Level grabLevel();

    Marker takeMarker();

    String fetchLoggerName();

    String takeMessage();

    String obtainThreadName();

    Object[] takeArgumentArray();

    long pullTimeStamp();

    Throwable takeThrowable();

}
