package net.roboticapex.senderReceivers;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;

public final class SenderReceiversConnection {

    public static final AttributeKey<SenderReceiversConnection> CONNECTION_ATTR = new AttributeKey<>("CONNECTION_ATTR");

    private final Channel channel;
    private final SenderReceiversPublicIdentity theirIdentity;

    /**
     * @param channel the netty channel to use for comms
     * @param theirIdentity the identity of the other side of this connection
     */
    public SenderReceiversConnection(Channel channel, SenderReceiversPublicIdentity theirIdentity) {
        this.channel = channel;
        this.theirIdentity = theirIdentity;
    }

    /**
     * Sends str to the other side of the connection
     * @param str
     * @throws SenderReceiversDeviation
     */
    public void write(String str) throws SenderReceiversDeviation {
        write(str.getBytes());
    }

    /**
     * Sends bytes to the other side of the connection
     * @param bytes
     * @throws SenderReceiversDeviation
     */
    public void write(byte[] bytes) throws SenderReceiversDeviation {
        channel.writeAndFlush(bytes);
    }

    /**
     * Closes the connection gracefully
     * @throws SenderReceiversDeviation
     */
    public void close() throws SenderReceiversDeviation {
        try {
            channel.close().sync();
        } catch (Exception e) {
            throw new SenderReceiversDeviation(e);
        }
    }

    public boolean isOpen() {
        return channel.isOpen();
    }

    /**
     * @return the identity of the other end of the connection
     */
    public SenderReceiversPublicIdentity obtainTheirIdentity() {
        return theirIdentity;
    }

    public String grabRemotePlaceString() {
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

        SenderReceiversConnection connection = (SenderReceiversConnection) o;
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
