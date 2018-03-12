package net.cybertip.note.helpers;

import net.cybertip.note.Marker;

public class SubstituteLoggerService {
    private final SubstituteLogger substituteLogger;

    public SubstituteLoggerService(SubstituteLogger substituteLogger) {
        this.substituteLogger = substituteLogger;
    }

    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        substituteLogger.delegate().trace(marker, format, arg1, arg2);
    }

    public boolean isDebugEnabled() {
        return substituteLogger.delegate().isDebugEnabled();
    }

    public void debug(String format, Object arg1, Object arg2) {
        substituteLogger.delegate().debug(format, arg1, arg2);
    }

    public void debug(String format, Object... arguments) {
        substituteLogger.delegate().debug(format, arguments);
    }

    public void debug(Marker marker, String msg) {
        substituteLogger.delegate().debug(marker, msg);
    }

    public void debug(Marker marker, String format, Object arg) {
        substituteLogger.delegate().debug(marker, format, arg);
    }

    public void debug(Marker marker, String msg, Throwable t) {
        substituteLogger.delegate().debug(marker, msg, t);
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

    public void info(Marker marker, String msg, Throwable t) {
        substituteLogger.delegate().info(marker, msg, t);
    }

    public boolean isWarnEnabled() {
        return substituteLogger.delegate().isWarnEnabled();
    }

    public void warn(String msg, Throwable t) {
        substituteLogger.delegate().warn(msg, t);
    }

    public boolean isErrorEnabled() {
        return substituteLogger.delegate().isErrorEnabled();
    }

    public void error(String format, Object arg1, Object arg2) {
        substituteLogger.delegate().error(format, arg1, arg2);
    }
}