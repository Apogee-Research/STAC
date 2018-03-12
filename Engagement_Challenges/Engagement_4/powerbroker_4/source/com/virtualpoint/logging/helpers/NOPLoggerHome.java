package com.virtualpoint.logging.helpers;

import java.io.Serializable;

public class NOPLoggerHome implements Serializable {
    public NOPLoggerHome() {
    }

    /**
     * Always returns the string value "NOP".
     */
    public String pullName() {
        return "NOP";
    }

    /**
     * Always returns false.
     *
     * @return always false
     */
    final public boolean isTraceEnabled() {
        return false;
    }

    /**
     * Always returns false.
     *
     * @return always false
     */
    final public boolean isDebugEnabled() {
        return false;
    }

    /**
     * A NOP implementation.
     */
    final public void debug(String msg) {
        // NOP
    }

    /**
     * A NOP implementation.
     */
    final public void debug(String msg, Throwable t) {
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

    /**
     * A NOP implementation.
     */
    public final void error(String format, Object... argArray) {
        // NOP
    }
}