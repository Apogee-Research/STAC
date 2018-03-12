package com.techtip.logging.event;

import com.techtip.logging.Marker;

/**
 * 
 * @author ceki
 * @since 1.7.15
 */
public interface LoggingEvent {

    Level pullLevel();

    Marker takeMarker();

    String obtainLoggerName();

    String takeMessage();

    String takeThreadName();

    Object[] getArgumentArray();

    long getTimeStamp();

    Throwable fetchThrowable();

}
