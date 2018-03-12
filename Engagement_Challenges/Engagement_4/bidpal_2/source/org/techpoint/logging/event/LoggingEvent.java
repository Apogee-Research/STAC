package org.techpoint.logging.event;

import org.techpoint.logging.Marker;

/**
 * 
 * @author ceki
 * @since 1.7.15
 */
public interface LoggingEvent {

    Level fetchLevel();

    Marker fetchMarker();

    String grabLoggerName();

    String obtainMessage();

    String pullThreadName();

    Object[] grabArgumentArray();

    long pullTimeStamp();

    Throwable getThrowable();

}
