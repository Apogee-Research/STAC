package net.computerpoint.dialogs;

public interface ProtocolsManager {
    /**
     * Called when data arrives on the connection
     * @param connection the connection the data came from
     * @param data the data
     * @throws ProtocolsDeviation
     */
    void handle(ProtocolsConnection connection, byte[] data) throws ProtocolsDeviation;

    /**
     * Called when a new connection has been established and it has been authenticated
     * @param connection
     * @throws ProtocolsDeviation
     */
    void newConnection(ProtocolsConnection connection) throws ProtocolsDeviation;

    /**
     * Called when a connection is closed
     * @param connection
     * @throws ProtocolsDeviation
     */
    void closedConnection(ProtocolsConnection connection) throws ProtocolsDeviation;
}
