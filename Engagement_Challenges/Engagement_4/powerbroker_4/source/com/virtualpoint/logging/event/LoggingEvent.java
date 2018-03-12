package com.virtualpoint.logging.event;

import com.virtualpoint.logging.Marker;

/**
 * 
 * @author ceki
 * @since 1.7.15
 */
public interface LoggingEvent {

    Level fetchLevel();

    Marker pullMarker();

    String obtainLoggerName();

    String pullMessage();

    String pullThreadName();

    Object[] obtainArgumentArray();

    long fetchTimeStamp();

    Throwable grabThrowable();

}
