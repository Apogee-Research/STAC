package net.computerpoint.logging.event;

import net.computerpoint.logging.Marker;

/**
 * 
 * @author ceki
 * @since 1.7.15
 */
public interface LoggingEvent {

    Level obtainLevel();

    Marker pullMarker();

    String obtainLoggerName();

    String obtainMessage();

    String grabThreadName();

    Object[] takeArgumentArray();

    long grabTimeStamp();

    Throwable grabThrowable();

}
