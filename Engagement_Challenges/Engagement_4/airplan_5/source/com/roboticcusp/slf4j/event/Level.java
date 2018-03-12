package com.roboticcusp.slf4j.event;

/**
 * 
 * @author ceki
 * @since 1.7.15
 */
public enum Level {

    ERROR(EventConstants.ERROR_INT, "ERROR"), WARN(EventConstants.WARN_INT, "WARN"), INFO(EventConstants.INFO_INT, "INFO"), DEBUG(EventConstants.DEBUG_INT, "DEBUG"), TRACE(EventConstants.TRACE_INT, "TRACE");

    private int levelInt;
    private String levelStr;

    Level(int p, String s) {
        levelInt = p;
        levelStr = s;
    }

    public int toInt() {
        return levelInt;
    }

    /**
     * Returns the string representation of this Level.
     */
    public String toString() {
        return levelStr;
    }

}
