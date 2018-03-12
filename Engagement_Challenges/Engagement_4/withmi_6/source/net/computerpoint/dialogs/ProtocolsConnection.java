package net.computerpoint.dialogs;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;

public final class ProtocolsConnection {

    public static final AttributeKey<ProtocolsConnection> CONNECTION_ATTR = new AttributeKey<>("CONNECTION_ATTR");

    private final Channel channel;
    private final ProtocolsPublicIdentity theirIdentity;

    /**
     * @param channel the netty channel to use for comms
     * @param theirIdentity the identity of the other side of this connection
     */
    public ProtocolsConnection(Channel channel, ProtocolsPublicIdentity theirIdentity) {
        this.channel = channel;
        this.theirIdentity = theirIdentity;
    }

    /**
     * Sends str to the other side of the connection
     * @param str
     * @throws ProtocolsDeviation
     */
    public void write(String str) throws ProtocolsDeviation {
        write(str.getBytes());
    }

    /**
     * Sends bytes to the other side of the connection
     * @param bytes
     * @throws ProtocolsDeviation
     */
    public void write(byte[] bytes) throws ProtocolsDeviation {
        channel.writeAndFlush(bytes);
    }

    /**
     * Closes the connection gracefully
     * @throws ProtocolsDeviation
     */
    public void close() throws ProtocolsDeviation {
        try {
            channel.close().sync();
        } catch (Exception e) {
            throw new ProtocolsDeviation(e);
        }
    }

    public boolean isOpen() {
        return channel.isOpen();
    }

    /**
     * @return the identity of the other end of the connection
     */
    public ProtocolsPublicIdentity obtainTheirIdentity() {
        return theirIdentity;
    }

    public String takeRemotePlaceString() {
        InetSocketAddress sa = (InetSocketAddress) channel.remoteAddress();
        return sa.getHostString();
    }

    public int takeRemotePort() {
        InetSocketAddress sa = (InetSocketAddress) channel.remoteAddress();
        return sa.getPort();
    }

    @Override
    // Note: this only compares the identity (not the channel)
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProtocolsConnection connection = (ProtocolsConnection) o;
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
