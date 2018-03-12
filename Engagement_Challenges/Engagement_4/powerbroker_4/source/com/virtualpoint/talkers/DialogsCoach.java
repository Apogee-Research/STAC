package com.virtualpoint.talkers;

public interface DialogsCoach {
    /**
     * Called when data arrives on the connection
     * @param connection the connection the data came from
     * @param data the data
     * @throws DialogsTrouble
     */
    void handle(DialogsConnection connection, byte[] data) throws DialogsTrouble;

    /**
     * Called when a new connection has been established and it has been authenticated
     * @param connection
     * @throws DialogsTrouble
     */
    void newConnection(DialogsConnection connection) throws DialogsTrouble;

    /**
     * Called when a connection is closed
     * @param connection
     * @throws DialogsTrouble
     */
    void closedConnection(DialogsConnection connection) throws DialogsTrouble;
}
