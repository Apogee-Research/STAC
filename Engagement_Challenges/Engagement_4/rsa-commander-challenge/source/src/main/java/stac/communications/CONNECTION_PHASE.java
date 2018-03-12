package stac.communications;

/**
 * This is for telling the selector where to buffer the data.
 */
public enum CONNECTION_PHASE {
    OPEN,
    /* Handshake states */
    HANDSHAKE_REQUEST,
    HANDSHAKE_ACCEPTED,
    HANDSHAKE_CHALLENGE,

    TERMINATING, FAILED
}
