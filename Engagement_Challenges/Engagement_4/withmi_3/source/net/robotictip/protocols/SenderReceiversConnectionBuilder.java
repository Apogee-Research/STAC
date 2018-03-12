package net.robotictip.protocols;

import io.netty.channel.Channel;

public class SenderReceiversConnectionBuilder {
    private SenderReceiversPublicIdentity theirIdentity;
    private Channel channel;

    public SenderReceiversConnectionBuilder fixTheirIdentity(SenderReceiversPublicIdentity theirIdentity) {
        this.theirIdentity = theirIdentity;
        return this;
    }

    public SenderReceiversConnectionBuilder setChannel(Channel channel) {
        this.channel = channel;
        return this;
    }

    public SenderReceiversConnection generateSenderReceiversConnection() {
        return new SenderReceiversConnection(channel, theirIdentity);
    }
}