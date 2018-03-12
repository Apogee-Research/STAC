package net.roboticapex.senderReceivers.internal;

import net.roboticapex.senderReceivers.SenderReceiversDeviation;
import net.roboticapex.senderReceivers.SenderReceiversHandler;
import net.roboticapex.senderReceivers.SenderReceiversIdentity;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

/**
 * Created by thennen on 2/5/16.
 */
public class SenderReceiversChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final SenderReceiversHandler handler;
    private final SenderReceiversIdentity identity;
    private final boolean isServer;
    private SenderReceiversNettyHandler nettyHandler;

    public SenderReceiversChannelInitializer(SenderReceiversHandler handler, SenderReceiversIdentity identity, boolean isServer) {
        this.handler = handler;
        this.identity = identity;
        this.isServer = isServer;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        // using the frame encoder and decoder should make it safe
        // to assume a 'read' will get the entire message...
        ch.pipeline()
                // TODO: allow the user to figure out what the max frame size is...
                .addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 4))
                .addLast("frameEncoder", new LengthFieldPrepender(4))
                .addLast("bytesEncoder", new ByteArrayEncoder())
                .addLast("bytesDecoder", new ByteArrayDecoder());

        // this is what does the authentication and encryption
        nettyHandler = new SenderReceiversNettyHandler(handler, identity, isServer, ch.newPromise());
        ch.pipeline().addLast(nettyHandler);
    }

    /**
     * Wait for authentication to complete
     * @param timeoutMillis amount of millis to wait for authentication
     * @throws SenderReceiversDeviation
     */
    public void awaitForAuthorize(long timeoutMillis) throws SenderReceiversDeviation {
        nettyHandler.awaitForAuthorize(timeoutMillis);
    }
}
