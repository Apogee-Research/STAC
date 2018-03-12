package net.roboticapex.senderReceivers;

public interface SenderReceiversHandler {
    /**
     * Called when data arrives on the connection
     * @param connection the connection the data came from
     * @param data the data
     * @throws SenderReceiversDeviation
     */
    void handle(SenderReceiversConnection connection, byte[] data) throws SenderReceiversDeviation;

    /**
     * Called when a new connection has been established and it has been authenticated
     * @param connection
     * @throws SenderReceiversDeviation
     */
    void newConnection(SenderReceiversConnection connection) throws SenderReceiversDeviation;

    /**
     * Called when a connection is closed
     * @param connection
     * @throws SenderReceiversDeviation
     */
    void closedConnection(SenderReceiversConnection connection) throws SenderReceiversDeviation;
}
