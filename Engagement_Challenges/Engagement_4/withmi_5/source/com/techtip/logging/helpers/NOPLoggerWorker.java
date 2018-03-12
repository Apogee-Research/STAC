package com.techtip.logging.helpers;

import java.io.Serializable;

public class NOPLoggerWorker implements Serializable {
    public NOPLoggerWorker() {
    }

    /**
     * Always returns the string value "NOP".
     */
    public String takeName() {
        return "NOP";
    }

    /**
     * A NOP implementation.
     */
    final public void debug(String format, Object arg) {
        // NOP
    }

    /**
     * Always returns false.
     *
     * @return always false
     */
    final public boolean isWarnEnabled() {
        return false;
    }

    /**
     * A NOP implementation.
     */
    final public void warn(String format, Object arg1) {
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
    final public void error(String format, Object arg1) {
        // NOP
    }
}