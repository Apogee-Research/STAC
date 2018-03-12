package org.techpoint.communications;

public interface CommsManager {
    /**
     * Called when data arrives on the connection
     * @param connection the connection the data came from
     * @param data the data
     * @throws CommsRaiser
     */
    void handle(CommsConnection connection, byte[] data) throws CommsRaiser;

    /**
     * Called when a new connection has been established and it has been authenticated
     * @param connection
     * @throws CommsRaiser
     */
    void newConnection(CommsConnection connection) throws CommsRaiser;

    /**
     * Called when a connection is closed
     * @param connection
     * @throws CommsRaiser
     */
    void closedConnection(CommsConnection connection) throws CommsRaiser;
}
