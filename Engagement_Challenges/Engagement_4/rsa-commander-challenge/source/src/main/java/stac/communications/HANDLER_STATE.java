package stac.communications;

/**
 * For telling the frame handler what the packet buffer is expecting.
 * Waiting means waiting for more data.
 * Failed means the connection should be terminated.
 * Close means the connection should be closed.
 * Done means the connection has finished.
 */
enum HANDLER_STATE {
    WAITING, FAILED, CLOSE, DONE
}
