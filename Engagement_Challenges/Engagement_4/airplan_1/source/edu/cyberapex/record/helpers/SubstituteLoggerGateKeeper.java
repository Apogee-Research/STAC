package edu.cyberapex.record.helpers;

import edu.cyberapex.record.Marker;

public class SubstituteLoggerGateKeeper {
    private final SubstituteLogger substituteLogger;

    public SubstituteLoggerGateKeeper(SubstituteLogger substituteLogger) {
        this.substituteLogger = substituteLogger;
    }

    public void trace(String msg) {
        substituteLogger.delegate().trace(msg);
    }

    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        substituteLogger.delegate().trace(marker, format, arg1, arg2);
    }

    public void debug(String format, Object arg1, Object arg2) {
        substituteLogger.delegate().debug(format, arg1, arg2);
    }

    public void debug(Marker marker, String msg) {
        substituteLogger.delegate().debug(marker, msg);
    }

    public void debug(Marker marker, String format, Object arg) {
        substituteLogger.delegate().debug(marker, format, arg);
    }

    public void info(String format, Object... arguments) {
        substituteLogger.delegate().info(format, arguments);
    }

    public void info(String msg, Throwable t) {
        substituteLogger.delegate().info(msg, t);
    }

    public void info(Marker marker, String format, Object arg) {
        substituteLogger.delegate().info(marker, format, arg);
    }

    public void info(Marker marker, String format, Object arg1, Object arg2) {
        substituteLogger.delegate().info(marker, format, arg1, arg2);
    }

    public boolean isWarnEnabled() {
        return substituteLogger.delegate().isWarnEnabled();
    }

    public void warn(String msg) {
        substituteLogger.delegate().warn(msg);
    }

    public void warn(String format, Object arg) {
        substituteLogger.delegate().warn(format, arg);
    }

    public void warn(String msg, Throwable t) {
        substituteLogger.delegate().warn(msg, t);
    }

    public boolean isWarnEnabled(Marker marker) {
        return substituteLogger.delegate().isWarnEnabled(marker);
    }

    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        substituteLogger.delegate().warn(marker, format, arg1, arg2);
    }
}