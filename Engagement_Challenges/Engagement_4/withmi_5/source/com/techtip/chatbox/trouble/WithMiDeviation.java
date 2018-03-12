package com.techtip.chatbox.trouble;

public class WithMiDeviation extends Exception {

    public WithMiDeviation() {
        super();
    }

    public WithMiDeviation(String message) {
        super(message);
    }

    public WithMiDeviation(String message, Throwable cause) {
        super(message, cause);
    }

    public WithMiDeviation(Throwable cause) {
        super(cause);
    }
}
