package org.techpoint.logging.event;

import static org.techpoint.logging.event.EventConstants.DEBUG_INT;
import static org.techpoint.logging.event.EventConstants.ERROR_INT;
import static org.techpoint.logging.event.EventConstants.INFO_INT;
import static org.techpoint.logging.event.EventConstants.TRACE_INT;
import static org.techpoint.logging.event.EventConstants.WARN_INT;

/**
 * 
 * @author ceki
 * @since 1.7.15
 */
public enum Level {

    ERROR(ERROR_INT, "ERROR"), WARN(WARN_INT, "WARN"), INFO(INFO_INT, "INFO"), DEBUG(DEBUG_INT, "DEBUG"), TRACE(TRACE_INT, "TRACE");

    private int levelInt;
    private String levelStr;

    Level(int q, String s) {
        levelInt = q;
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
