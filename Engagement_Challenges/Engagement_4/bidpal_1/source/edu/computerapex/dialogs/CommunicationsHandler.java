package edu.computerapex.dialogs;

public interface CommunicationsHandler {
    /**
     * Called when data arrives on the connection
     * @param connection the connection the data came from
     * @param data the data
     * @throws CommunicationsDeviation
     */
    void handle(CommunicationsConnection connection, byte[] data) throws CommunicationsDeviation;

    /**
     * Called when a new connection has been established and it has been authenticated
     * @param connection
     * @throws CommunicationsDeviation
     */
    void newConnection(CommunicationsConnection connection) throws CommunicationsDeviation;

    /**
     * Called when a connection is closed
     * @param connection
     * @throws CommunicationsDeviation
     */
    void closedConnection(CommunicationsConnection connection) throws CommunicationsDeviation;
}
