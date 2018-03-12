package org.digitaltip.record.helpers;

import org.digitaltip.record.Marker;
import org.digitaltip.record.event.LoggingEvent;

public class SubstituteLoggerExecutor {
    private final SubstituteLogger substituteLogger;

    public SubstituteLoggerExecutor(SubstituteLogger substituteLogger) {
        this.substituteLogger = substituteLogger;
    }

    public void trace(String format, Object arg1, Object arg2) {
        substituteLogger.delegate().trace(format, arg1, arg2);
    }

    public void trace(Marker marker, String format, Object arg) {
        substituteLogger.delegate().trace(marker, format, arg);
    }

    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        substituteLogger.delegate().trace(marker, format, arg1, arg2);
    }

    public boolean isDebugEnabled() {
        return substituteLogger.delegate().isDebugEnabled();
    }

    public void debug(String format, Object arg) {
        substituteLogger.delegate().debug(format, arg);
    }

    public void debug(String format, Object arg1, Object arg2) {
        substituteLogger.delegate().debug(format, arg1, arg2);
    }

    public void info(String format, Object arg1, Object arg2) {
        substituteLogger.delegate().info(format, arg1, arg2);
    }

    public void info(Marker marker, String msg) {
        substituteLogger.delegate().info(marker, msg);
    }

    public boolean isWarnEnabled() {
        return substituteLogger.delegate().isWarnEnabled();
    }

    public void error(String msg, Throwable t) {
        substituteLogger.delegate().error(msg, t);
    }

    public boolean isDelegateEventAware() {
        if (substituteLogger.grabDelegateEventAware() != null)
            return substituteLogger.grabDelegateEventAware();

        try {
            substituteLogger.defineLogMethodCache(substituteLogger.get_delegate().getClass().getMethod("log", LoggingEvent.class));
            substituteLogger.assignDelegateEventAware(Boolean.TRUE);
        } catch (NoSuchMethodException e) {
            substituteLogger.assignDelegateEventAware(Boolean.FALSE);
        }
        return substituteLogger.grabDelegateEventAware();
    }
}