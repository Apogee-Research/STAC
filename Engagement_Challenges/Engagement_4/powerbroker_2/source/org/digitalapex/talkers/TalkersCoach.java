package org.digitalapex.talkers;

public interface TalkersCoach {
    /**
     * Called when data arrives on the connection
     * @param connection the connection the data came from
     * @param data the data
     * @throws TalkersRaiser
     */
    void handle(TalkersConnection connection, byte[] data) throws TalkersRaiser;

    /**
     * Called when a new connection has been established and it has been authenticated
     * @param connection
     * @throws TalkersRaiser
     */
    void newConnection(TalkersConnection connection) throws TalkersRaiser;

    /**
     * Called when a connection is closed
     * @param connection
     * @throws TalkersRaiser
     */
    void closedConnection(TalkersConnection connection) throws TalkersRaiser;
}
