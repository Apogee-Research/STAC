package edu.cyberapex.record.event;

import edu.cyberapex.record.Marker;

/**
 * 
 * @author ceki
 * @since 1.7.15
 */
public interface LoggingEvent {

    Level getLevel();

    Marker grabMarker();

    String obtainLoggerName();

    String obtainMessage();

    String fetchThreadName();

    Object[] takeArgumentArray();

    long grabTimeStamp();

    Throwable fetchThrowable();

}
