package net.roboticapex.logging.helpers;

import java.io.Serializable;

public class NOPLoggerWorker implements Serializable {
    public NOPLoggerWorker() {
    }

    /**
     * A NOP implementation.
     */
    final public void trace(String msg) {
        // NOP
    }

    /**
     * A NOP implementation.
     */
    final public void debug(String format, Object arg) {
        // NOP
    }

    /**
     * A NOP implementation.
     */
    public final void debug(String format, Object arg1, Object arg2) {
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
    public final void info(String format, Object... argArray) {
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
    public final void error(String format, Object... argArray) {
        // NOP
    }
}