package edu.networkcusp.protocols;

public interface CommunicationsGuide {
    /**
     * Called when data arrives on the connection
     * @param connection the connection the data came from
     * @param data the data
     * @throws CommunicationsFailure
     */
    void handle(CommunicationsConnection connection, byte[] data) throws CommunicationsFailure;

    /**
     * Called when a new connection has been established and it has been authenticated
     * @param connection
     * @throws CommunicationsFailure
     */
    void newConnection(CommunicationsConnection connection) throws CommunicationsFailure;

    /**
     * Called when a connection is closed
     * @param connection
     * @throws CommunicationsFailure
     */
    void closedConnection(CommunicationsConnection connection) throws CommunicationsFailure;
}
