package net.techpoint.note.event;

import net.techpoint.note.Marker;

/**
 * 
 * @author ceki
 * @since 1.7.15
 */
public interface LoggingEvent {

    Level obtainLevel();

    Marker takeMarker();

    String getLoggerName();

    String pullMessage();

    String pullThreadName();

    Object[] grabArgumentArray();

    long obtainTimeStamp();

    Throwable pullThrowable();

}
