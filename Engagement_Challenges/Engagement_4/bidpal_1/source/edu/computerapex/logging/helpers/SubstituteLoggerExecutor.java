package edu.computerapex.logging.helpers;

import edu.computerapex.logging.Marker;

public class SubstituteLoggerExecutor {
    private final SubstituteLogger substituteLogger;

    public SubstituteLoggerExecutor(SubstituteLogger substituteLogger) {
        this.substituteLogger = substituteLogger;
    }

    public void trace(String msg) {
        substituteLogger.delegate().trace(msg);
    }

    public void trace(String format, Object arg1, Object arg2) {
        substituteLogger.delegate().trace(format, arg1, arg2);
    }

    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        substituteLogger.delegate().trace(marker, format, arg1, arg2);
    }

    public void debug(Marker marker, String msg) {
        substituteLogger.delegate().debug(marker, msg);
    }

    public void debug(Marker marker, String format, Object arg) {
        substituteLogger.delegate().debug(marker, format, arg);
    }

    public void debug(Marker marker, String format, Object... arguments) {
        substituteLogger.delegate().debug(marker, format, arguments);
    }

    public void debug(Marker marker, String msg, Throwable t) {
        substituteLogger.delegate().debug(marker, msg, t);
    }

    public void info(String format, Object... arguments) {
        substituteLogger.delegate().info(format, arguments);
    }

    public boolean isInfoEnabled(Marker marker) {
        return substituteLogger.delegate().isInfoEnabled(marker);
    }

    public void info(Marker marker, String msg, Throwable t) {
        substituteLogger.delegate().info(marker, msg, t);
    }

    public void warn(String format, Object arg1, Object arg2) {
        substituteLogger.delegate().warn(format, arg1, arg2);
    }

    public void warn(String format, Object... arguments) {
        substituteLogger.delegate().warn(format, arguments);
    }

    public boolean isWarnEnabled(Marker marker) {
        return substituteLogger.delegate().isWarnEnabled(marker);
    }

    public void warn(Marker marker, String msg, Throwable t) {
        substituteLogger.delegate().warn(marker, msg, t);
    }

    public void error(String format, Object... arguments) {
        substituteLogger.delegate().error(format, arguments);
    }

    public boolean isErrorEnabled(Marker marker) {
        return substituteLogger.delegate().isErrorEnabled(marker);
    }

    public void error(Marker marker, String format, Object arg1, Object arg2) {
        substituteLogger.delegate().error(marker, format, arg1, arg2);
    }

    public void error(Marker marker, String format, Object... arguments) {
        substituteLogger.delegate().error(marker, format, arguments);
    }
}