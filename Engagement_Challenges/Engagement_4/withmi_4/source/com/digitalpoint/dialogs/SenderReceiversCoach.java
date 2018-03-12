package com.digitalpoint.dialogs;

public interface SenderReceiversCoach {
    /**
     * Called when data arrives on the connection
     * @param connection the connection the data came from
     * @param data the data
     * @throws SenderReceiversException
     */
    void handle(SenderReceiversConnection connection, byte[] data) throws SenderReceiversException;

    /**
     * Called when a new connection has been established and it has been authenticated
     * @param connection
     * @throws SenderReceiversException
     */
    void newConnection(SenderReceiversConnection connection) throws SenderReceiversException;

    /**
     * Called when a connection is closed
     * @param connection
     * @throws SenderReceiversException
     */
    void closedConnection(SenderReceiversConnection connection) throws SenderReceiversException;
}
