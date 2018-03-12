package edu.networkcusp.chatbox.exception;

public class WithMiFailure extends Exception {

    public WithMiFailure() {
        super();
    }

    public WithMiFailure(String message) {
        super(message);
    }

    public WithMiFailure(String message, Throwable cause) {
        super(message, cause);
    }

    public WithMiFailure(Throwable cause) {
        super(cause);
    }
}
