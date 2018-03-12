package net.roboticapex.logging.event;

import net.roboticapex.logging.Marker;

/**
 * 
 * @author ceki
 * @since 1.7.15
 */
public interface LoggingEvent {

    Level fetchLevel();

    Marker obtainMarker();

    String grabLoggerName();

    String takeMessage();

    String fetchThreadName();

    Object[] fetchArgumentArray();

    long pullTimeStamp();

    Throwable takeThrowable();

}
