package org.techpoint.communications.internal;

import org.techpoint.communications.CommsRaiser;
import org.techpoint.communications.CommsManager;
import org.techpoint.communications.CommsIdentity;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

/**
 * Created by thennen on 2/5/16.
 */
public class CommsChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final CommsManager manager;
    private final CommsIdentity identity;
    private final boolean isServer;
    private CommsNettyManager nettyManager;

    public CommsChannelInitializer(CommsManager coordinator, CommsIdentity identity, boolean isServer) {
        this.manager = coordinator;
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
        nettyManager = new CommsNettyManager(manager, identity, isServer, ch.newPromise());
        ch.pipeline().addLast(nettyManager);
    }

    /**
     * Wait for authentication to complete
     * @param timeoutMillis amount of millis to wait for authentication
     * @throws CommsRaiser
     */
    public void awaitForAuth(long timeoutMillis) throws CommsRaiser {
        nettyManager.awaitForAuth(timeoutMillis);
    }
}
