package com.virtualpoint.talkers;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public final class DialogsConnection {

    public static final AttributeKey<DialogsConnection> CONNECTION_ATTR = new AttributeKey<>("CONNECTION_ATTR");

    private final Channel channel;
    private final DialogsPublicIdentity theirIdentity;
    private final DialogsConnectionHelper dialogsConnectionHelper = new DialogsConnectionHelper(this);

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
     * @throws DialogsTrouble
     */
    public void write(String str) throws DialogsTrouble {
        dialogsConnectionHelper.write(str);
    }

    /**
     * Sends bytes to the other side of the connection
     * @param bytes
     * @throws DialogsTrouble
     */
    public void write(byte[] bytes) throws DialogsTrouble {
        channel.writeAndFlush(bytes);
    }

    /**
     * Closes the connection gracefully
     * @throws DialogsTrouble
     */
    public void close() throws DialogsTrouble {
        try {
            channel.close().sync();
        } catch (Exception e) {
            throw new DialogsTrouble(e);
        }
    }

    public boolean isOpen() {
        return dialogsConnectionHelper.isOpen();
    }

    /**
     * @return the identity of the other end of the connection
     */
    public DialogsPublicIdentity pullTheirIdentity() {
        return theirIdentity;
    }

    public String fetchRemotePlaceString() {
        return dialogsConnectionHelper.obtainRemotePlaceString();
    }

    public int getRemotePort() {
        return dialogsConnectionHelper.obtainRemotePort();
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

    public Channel fetchChannel() {
        return channel;
    }
}
