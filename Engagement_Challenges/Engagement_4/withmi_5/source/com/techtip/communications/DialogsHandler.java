package com.techtip.communications;

public interface DialogsHandler {
    /**
     * Called when data arrives on the connection
     * @param connection the connection the data came from
     * @param data the data
     * @throws DialogsDeviation
     */
    void handle(DialogsConnection connection, byte[] data) throws DialogsDeviation;

    /**
     * Called when a new connection has been established and it has been authenticated
     * @param connection
     * @throws DialogsDeviation
     */
    void newConnection(DialogsConnection connection) throws DialogsDeviation;

    /**
     * Called when a connection is closed
     * @param connection
     * @throws DialogsDeviation
     */
    void closedConnection(DialogsConnection connection) throws DialogsDeviation;
}
