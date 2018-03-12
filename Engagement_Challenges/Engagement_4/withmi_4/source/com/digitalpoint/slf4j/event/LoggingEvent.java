package com.digitalpoint.slf4j.event;

import com.digitalpoint.slf4j.Marker;

/**
 * 
 * @author ceki
 * @since 1.7.15
 */
public interface LoggingEvent {

    Level getLevel();

    Marker takeMarker();

    String grabLoggerName();

    String fetchMessage();

    String pullThreadName();

    Object[] grabArgumentArray();

    long takeTimeStamp();

    Throwable pullThrowable();

}
