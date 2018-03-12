package net.robotictip.protocols;

public interface SenderReceiversManager {
    /**
     * Called when data arrives on the connection
     * @param connection the connection the data came from
     * @param data the data
     * @throws SenderReceiversTrouble
     */
    void handle(SenderReceiversConnection connection, byte[] data) throws SenderReceiversTrouble;

    /**
     * Called when a new connection has been established and it has been authenticated
     * @param connection
     * @throws SenderReceiversTrouble
     */
    void newConnection(SenderReceiversConnection connection) throws SenderReceiversTrouble;

    /**
     * Called when a connection is closed
     * @param connection
     * @throws SenderReceiversTrouble
     */
    void closedConnection(SenderReceiversConnection connection) throws SenderReceiversTrouble;
}
