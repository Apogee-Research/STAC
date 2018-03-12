package org.digitaltip.dialogs;

public interface TalkersGuide {
    /**
     * Called when data arrives on the connection
     * @param connection the connection the data came from
     * @param data the data
     * @throws TalkersDeviation
     */
    void handle(TalkersConnection connection, byte[] data) throws TalkersDeviation;

    /**
     * Called when a new connection has been established and it has been authenticated
     * @param connection
     * @throws TalkersDeviation
     */
    void newConnection(TalkersConnection connection) throws TalkersDeviation;

    /**
     * Called when a connection is closed
     * @param connection
     * @throws TalkersDeviation
     */
    void closedConnection(TalkersConnection connection) throws TalkersDeviation;
}
