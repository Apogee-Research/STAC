package net.robotictip.dropbys.failure;

public class WithMiException extends Exception {

    public WithMiException() {
        super();
    }

    public WithMiException(String message) {
        super(message);
    }

    public WithMiException(String message, Throwable cause) {
        super(message, cause);
    }

    public WithMiException(Throwable cause) {
        super(cause);
    }
}
