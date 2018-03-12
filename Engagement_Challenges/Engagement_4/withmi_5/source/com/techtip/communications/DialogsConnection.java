package com.techtip.communications;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;

public final class DialogsConnection {

    public static final AttributeKey<DialogsConnection> CONNECTION_ATTR = new AttributeKey<>("CONNECTION_ATTR");

    private final Channel channel;
    private final DialogsPublicIdentity theirIdentity;

    /**
     * @param channel the netty channel to use for comms
     * @param theirIdentity the identity of the other side of this connection
     */
    public DialogsConnection(Channel channel, DialogsPublicIdentity theirIdentity) {
        this.channel = channel;
        this.theirIdentity = theirIdentity;
    }

    /**
     * Sends str to the other side of the connection
     * @param str
     * @throws DialogsDeviation
     */
    public void write(String str) throws DialogsDeviation {
        write(str.getBytes());
    }

    /**
     * Sends bytes to the other side of the connection
     * @param bytes
     * @throws DialogsDeviation
     */
    public void write(byte[] bytes) throws DialogsDeviation {
        channel.writeAndFlush(bytes);
    }

    /**
     * Closes the connection gracefully
     * @throws DialogsDeviation
     */
    public void close() throws DialogsDeviation {
        try {
            channel.close().sync();
        } catch (Exception e) {
            throw new DialogsDeviation(e);
        }
    }

    public boolean isOpen() {
        return channel.isOpen();
    }

    /**
     * @return the identity of the other end of the connection
     */
    public DialogsPublicIdentity fetchTheirIdentity() {
        return theirIdentity;
    }

    public String takeRemoteOriginString() {
        InetSocketAddress sa = (InetSocketAddress) channel.remoteAddress();
        return sa.getHostString();
    }

    public int getRemotePort() {
        InetSocketAddress sa = (InetSocketAddress) channel.remoteAddress();
        return sa.getPort();
    }

    @Override
    // Note: this only compares the identity (not the channel)
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DialogsConnection connection = (DialogsConnection) o;
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
