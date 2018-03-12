package net.cybertip.note.helpers;

import java.io.Serializable;

public class NOPLoggerSupervisor implements Serializable {
    public NOPLoggerSupervisor() {
    }

    /**
     * A NOP implementation.
     */
    public final void trace(String format, Object arg1, Object arg2) {
        // NOP
    }

    /**
     * A NOP implementation.
     */
    final public void debug(String msg) {
        // NOP
    }

    /**
     * Always returns false.
     *
     * @return always false
     */
    final public boolean isInfoEnabled() {
        // NOP
        return false;
    }

    /**
     * A NOP implementation.
     */
    final public void info(String msg) {
        // NOP
    }

    /**
     * A NOP implementation.
     */
    public final void info(String format, Object... argArray) {
        // NOP
    }

    /**
     * A NOP implementation.
     */
    final public void warn(String format, Object arg1, Object arg2) {
        // NOP
    }

    /**
     * A NOP implementation.
     */
    public final void warn(String format, Object... argArray) {
        // NOP
    }

    /**
     * A NOP implementation.
     */
    final public boolean isErrorEnabled() {
        return false;
    }

    /**
     * A NOP implementation.
     */
    final public void error(String msg) {
        // NOP
    }
}