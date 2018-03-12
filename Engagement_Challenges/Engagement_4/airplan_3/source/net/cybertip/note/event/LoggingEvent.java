package net.cybertip.note.event;

import net.cybertip.note.Marker;

/**
 * 
 * @author ceki
 * @since 1.7.15
 */
public interface LoggingEvent {

    Level grabLevel();

    Marker fetchMarker();

    String takeLoggerName();

    String pullMessage();

    String takeThreadName();

    Object[] grabArgumentArray();

    long getTimeStamp();

    Throwable fetchThrowable();

}
