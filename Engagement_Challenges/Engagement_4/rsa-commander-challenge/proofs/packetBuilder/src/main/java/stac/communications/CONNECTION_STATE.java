package stac.communications;

/**
 *
 */
public enum CONNECTION_STATE {
    OPEN,
    /* Handshake states */
    HANDSHAKE_REQUEST,
    HANDSHAKE_ACCEPTED,
    HANDSHAKE_CHALLENGE,

    TERMINATING, FAILED
}
