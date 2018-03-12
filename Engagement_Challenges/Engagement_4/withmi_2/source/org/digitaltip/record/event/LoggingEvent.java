package org.digitaltip.record.event;

import org.digitaltip.record.Marker;

/**
 * 
 * @author ceki
 * @since 1.7.15
 */
public interface LoggingEvent {

    Level obtainLevel();

    Marker getMarker();

    String grabLoggerName();

    String getMessage();

    String pullThreadName();

    Object[] pullArgumentArray();

    long pullTimeStamp();

    Throwable obtainThrowable();

}
