package stac.communications;

/**
 * This exception is thrown when parsers encounter errors that they cannot
 * recover from. This error indicates that the packet object is in a broken
 * state and the data within should not be relied upon.
 */
public class PacketParserException extends Exception {
    private static final long serialVersionUID = -6311931030088312913L;
    private final String message;

    public PacketParserException(String message) {
        this.message = message;
    }

    /**
     * Returns the detail message string of this throwable.
     *
     * @return the detail message string of this {@code Throwable} instance
     * (which may be {@code null}).
     */
    @Override
    public String getMessage() {
        return message;
    }
}
