package edu.computerapex.logging.event;

import edu.computerapex.logging.Marker;

/**
 * 
 * @author ceki
 * @since 1.7.15
 */
public interface LoggingEvent {

    Level fetchLevel();

    Marker fetchMarker();

    String getLoggerName();

    String fetchMessage();

    String pullThreadName();

    Object[] grabArgumentArray();

    long takeTimeStamp();

    Throwable pullThrowable();

}
