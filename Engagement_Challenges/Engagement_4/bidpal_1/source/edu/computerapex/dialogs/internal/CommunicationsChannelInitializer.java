package edu.computerapex.dialogs.internal;

import edu.computerapex.dialogs.CommunicationsDeviation;
import edu.computerapex.dialogs.CommunicationsHandler;
import edu.computerapex.dialogs.CommunicationsIdentity;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

/**
 * Created by thennen on 2/5/16.
 */
public class CommunicationsChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final CommunicationsHandler handler;
    private final CommunicationsIdentity identity;
    private final boolean isServer;
    private CommunicationsNettyHandler nettyHandler;

    public CommunicationsChannelInitializer(CommunicationsHandler handler, CommunicationsIdentity identity, boolean isServer) {
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
        nettyHandler = new CommunicationsNettyHandler(handler, identity, isServer, ch.newPromise());
        ch.pipeline().addLast(nettyHandler);
    }

    /**
     * Wait for authentication to complete
     * @param timeoutMillis amount of millis to wait for authentication
     * @throws CommunicationsDeviation
     */
    public void awaitForPermission(long timeoutMillis) throws CommunicationsDeviation {
        nettyHandler.awaitForPermission(timeoutMillis);
    }
}
