package stac.communications;

/**
 * This defines the minimum and maximum sizes of the packets this instance
 * expects to see. -1 means unlimited.
 */
public enum PACKETS {
    HANDSHAKE_OPEN(34, 131108), REQUEST(77, -1),
    HANDSHAKE_CHALLENGE(4, 2048),
    REQUEST_MESSAGE(128, -1), REQUEST_MESSAGE_RELAY(128, -1),
    RETRIEVE_MESSAGES(0, -1),
    REQUEST_TERMINATE(128, -1);

    private final int max, min;

    PACKETS(int minSize, int maxSize) {
        this.max = maxSize;
        this.min = minSize;
    }

    public int maxSize() {
        return max;
    }

    public int minSize() {
        return min;
    }
}
