package net.robotictip.slf4j.event;

import net.robotictip.slf4j.Marker;

/**
 * 
 * @author ceki
 * @since 1.7.15
 */
public interface LoggingEvent {

    Level fetchLevel();

    Marker getMarker();

    String fetchLoggerName();

    String obtainMessage();

    String takeThreadName();

    Object[] obtainArgumentArray();

    long fetchTimeStamp();

    Throwable getThrowable();

}
