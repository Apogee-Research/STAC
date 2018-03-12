package org.digitaltip.record.helpers;

import java.io.Serializable;

public class NOPLoggerHelper implements Serializable {
    public NOPLoggerHelper() {
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
    final public void warn(String msg, Throwable t) {
        // NOP
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
    final public void error(String format, Object arg1) {
        // NOP
    }
}