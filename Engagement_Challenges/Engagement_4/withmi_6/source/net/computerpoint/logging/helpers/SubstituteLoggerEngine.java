package net.computerpoint.logging.helpers;

import net.computerpoint.logging.Marker;

public class SubstituteLoggerEngine {
    private final SubstituteLogger substituteLogger;

    public SubstituteLoggerEngine(SubstituteLogger substituteLogger) {
        this.substituteLogger = substituteLogger;
    }

    public void trace(String msg) {
        substituteLogger.delegate().trace(msg);
    }

    public void trace(Marker marker, String msg) {
        substituteLogger.delegate().trace(marker, msg);
    }

    public void trace(Marker marker, String format, Object arg) {
        substituteLogger.delegate().trace(marker, format, arg);
    }

    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        substituteLogger.delegate().trace(marker, format, arg1, arg2);
    }

    public void trace(Marker marker, String format, Object... arguments) {
        substituteLogger.delegate().trace(marker, format, arguments);
    }

    public void debug(Marker marker, String format, Object... arguments) {
        substituteLogger.delegate().debug(marker, format, arguments);
    }

    public void debug(Marker marker, String msg, Throwable t) {
        substituteLogger.delegate().debug(marker, msg, t);
    }

    public void info(Marker marker, String format, Object arg) {
        substituteLogger.delegate().info(marker, format, arg);
    }

    public void info(Marker marker, String format, Object arg1, Object arg2) {
        substituteLogger.delegate().info(marker, format, arg1, arg2);
    }

    public void info(Marker marker, String msg, Throwable t) {
        substituteLogger.delegate().info(marker, msg, t);
    }

    public boolean isWarnEnabled(Marker marker) {
        return substituteLogger.delegate().isWarnEnabled(marker);
    }

    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        substituteLogger.delegate().warn(marker, format, arg1, arg2);
    }

    public void warn(Marker marker, String format, Object... arguments) {
        substituteLogger.delegate().warn(marker, format, arguments);
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

    public void error(Marker marker, String msg, Throwable t) {
        substituteLogger.delegate().error(marker, msg, t);
    }
}