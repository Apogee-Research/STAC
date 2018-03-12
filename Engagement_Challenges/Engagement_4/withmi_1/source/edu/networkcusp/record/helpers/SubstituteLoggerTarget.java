package edu.networkcusp.record.helpers;

import edu.networkcusp.record.Marker;

public class SubstituteLoggerTarget {
    private final SubstituteLogger substituteLogger;

    public SubstituteLoggerTarget(SubstituteLogger substituteLogger) {
        this.substituteLogger = substituteLogger;
    }

    public void trace(String format, Object... arguments) {
        substituteLogger.delegate().trace(format, arguments);
    }

    public void trace(Marker marker, String format, Object arg) {
        substituteLogger.delegate().trace(marker, format, arg);
    }

    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        substituteLogger.delegate().trace(marker, format, arg1, arg2);
    }

    public void debug(String msg) {
        substituteLogger.delegate().debug(msg);
    }

    public void debug(String format, Object arg1, Object arg2) {
        substituteLogger.delegate().debug(format, arg1, arg2);
    }

    public boolean isDebugEnabled(Marker marker) {
        return substituteLogger.delegate().isDebugEnabled(marker);
    }

    public void debug(Marker marker, String msg) {
        substituteLogger.delegate().debug(marker, msg);
    }

    public boolean isInfoEnabled() {
        return substituteLogger.delegate().isInfoEnabled();
    }

    public void info(String msg) {
        substituteLogger.delegate().info(msg);
    }

    public void info(String msg, Throwable t) {
        substituteLogger.delegate().info(msg, t);
    }

    public void warn(String format, Object arg) {
        substituteLogger.delegate().warn(format, arg);
    }

    public void warn(String format, Object arg1, Object arg2) {
        substituteLogger.delegate().warn(format, arg1, arg2);
    }

    public boolean isWarnEnabled(Marker marker) {
        return substituteLogger.delegate().isWarnEnabled(marker);
    }

    public void warn(Marker marker, String msg) {
        substituteLogger.delegate().warn(marker, msg);
    }

    public void warn(Marker marker, String format, Object arg) {
        substituteLogger.delegate().warn(marker, format, arg);
    }

    public void error(String msg) {
        substituteLogger.delegate().error(msg);
    }

    public void error(String format, Object arg) {
        substituteLogger.delegate().error(format, arg);
    }

    public void error(Marker marker, String msg) {
        substituteLogger.delegate().error(marker, msg);
    }

    public void error(Marker marker, String format, Object arg) {
        substituteLogger.delegate().error(marker, format, arg);
    }

    public void error(Marker marker, String msg, Throwable t) {
        substituteLogger.delegate().error(marker, msg, t);
    }
}