package edu.networkcusp.protocols;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;

public final class CommunicationsConnection {

    public static final AttributeKey<CommunicationsConnection> CONNECTION_ATTR = new AttributeKey<>("CONNECTION_ATTR");

    private final Channel channel;
    private final CommunicationsPublicIdentity theirIdentity;

    /**
     * @param channel the netty channel to use for comms
     * @param theirIdentity the identity of the other side of this connection
     */
    public CommunicationsConnection(Channel channel, CommunicationsPublicIdentity theirIdentity) {
        this.channel = channel;
        this.theirIdentity = theirIdentity;
    }

    /**
     * Sends str to the other side of the connection
     * @param str
     * @throws CommunicationsFailure
     */
    public void write(String str) throws CommunicationsFailure {
        write(str.getBytes());
    }

    /**
     * Sends bytes to the other side of the connection
     * @param bytes
     * @throws CommunicationsFailure
     */
    public void write(byte[] bytes) throws CommunicationsFailure {
        channel.writeAndFlush(bytes);
    }

    /**
     * Closes the connection gracefully
     * @throws CommunicationsFailure
     */
    public void close() throws CommunicationsFailure {
        try {
            channel.close().sync();
        } catch (Exception e) {
            throw new CommunicationsFailure(e);
        }
    }

    public boolean isOpen() {
        return channel.isOpen();
    }

    /**
     * @return the identity of the other end of the connection
     */
    public CommunicationsPublicIdentity fetchTheirIdentity() {
        return theirIdentity;
    }

    public String pullRemoteHostString() {
        InetSocketAddress sa = (InetSocketAddress) channel.remoteAddress();
        return sa.getHostString();
    }

    public int grabRemotePort() {
        InetSocketAddress sa = (InetSocketAddress) channel.remoteAddress();
        return sa.getPort();
    }

    @Override
    // Note: this only compares the identity (not the channel)
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommunicationsConnection connection = (CommunicationsConnection) o;
        if (!theirIdentity.equals(connection.theirIdentity)) return false;

        return true;

    }

    @Override
    // Note: this only uses the identity
    public int hashCode() {
        int result = theirIdentity.hashCode();
        return result;
    }

}
