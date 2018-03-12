package org.digitalapex.logging.event;

import org.digitalapex.logging.Marker;

/**
 * 
 * @author ceki
 * @since 1.7.15
 */
public interface LoggingEvent {

    Level getLevel();

    Marker obtainMarker();

    String obtainLoggerName();

    String fetchMessage();

    String obtainThreadName();

    Object[] grabArgumentArray();

    long obtainTimeStamp();

    Throwable getThrowable();

}
