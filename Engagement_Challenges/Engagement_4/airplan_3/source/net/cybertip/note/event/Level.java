package net.cybertip.note.event;

import static net.cybertip.note.event.EventConstants.DEBUG_INT;
import static net.cybertip.note.event.EventConstants.ERROR_INT;
import static net.cybertip.note.event.EventConstants.INFO_INT;
import static net.cybertip.note.event.EventConstants.TRACE_INT;
import static net.cybertip.note.event.EventConstants.WARN_INT;

/**
 * 
 * @author ceki
 * @since 1.7.15
 */
public enum Level {

    ERROR(ERROR_INT, "ERROR"), WARN(WARN_INT, "WARN"), INFO(INFO_INT, "INFO"), DEBUG(DEBUG_INT, "DEBUG"), TRACE(TRACE_INT, "TRACE");

    private int levelInt;
    private String levelStr;

    Level(int c, String s) {
        levelInt = c;
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
