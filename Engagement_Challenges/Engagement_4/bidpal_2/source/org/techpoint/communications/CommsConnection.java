package org.techpoint.communications;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;

public final class CommsConnection {

    public static final AttributeKey<CommsConnection> CONNECTION_ATTR = new AttributeKey<>("CONNECTION_ATTR");

    private final Channel channel;
    private final CommsPublicIdentity theirIdentity;

    /**
     * @param channel the netty channel to use for comms
     * @param theirIdentity the identity of the other side of this connection
     */
    public CommsConnection(Channel channel, CommsPublicIdentity theirIdentity) {
        this.channel = channel;
        this.theirIdentity = theirIdentity;
    }

    /**
     * Sends str to the other side of the connection
     * @param str
     * @throws CommsRaiser
     */
    public void write(String str) throws CommsRaiser {
        write(str.getBytes());
    }

    /**
     * Sends bytes to the other side of the connection
     * @param bytes
     * @throws CommsRaiser
     */
    public void write(byte[] bytes) throws CommsRaiser {
        channel.writeAndFlush(bytes);
    }

    /**
     * Closes the connection gracefully
     * @throws CommsRaiser
     */
    public void close() throws CommsRaiser {
        try {
            channel.close().sync();
        } catch (Exception e) {
            throw new CommsRaiser(e);
        }
    }

    public boolean isOpen() {
        return channel.isOpen();
    }

    /**
     * @return the identity of the other end of the connection
     */
    public CommsPublicIdentity obtainTheirIdentity() {
        return theirIdentity;
    }

    public String takeRemotePlaceString() {
        InetSocketAddress sa = (InetSocketAddress) channel.remoteAddress();
        return sa.getHostString();
    }

    public int pullRemotePort() {
        InetSocketAddress sa = (InetSocketAddress) channel.remoteAddress();
        return sa.getPort();
    }

    @Override
    // Note: this only compares the identity (not the channel)
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommsConnection connection = (CommsConnection) o;
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
