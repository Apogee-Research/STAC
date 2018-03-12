package edu.networkcusp.senderReceivers;

public interface ProtocolsHandler {
    /**
     * Called when data arrives on the connection
     * @param connection the connection the data came from
     * @param data the data
     * @throws ProtocolsRaiser
     */
    void handle(ProtocolsConnection connection, byte[] data) throws ProtocolsRaiser;

    /**
     * Called when a new connection has been established and it has been authenticated
     * @param connection
     * @throws ProtocolsRaiser
     */
    void newConnection(ProtocolsConnection connection) throws ProtocolsRaiser;

    /**
     * Called when a connection is closed
     * @param connection
     * @throws ProtocolsRaiser
     */
    void closedConnection(ProtocolsConnection connection) throws ProtocolsRaiser;
}
