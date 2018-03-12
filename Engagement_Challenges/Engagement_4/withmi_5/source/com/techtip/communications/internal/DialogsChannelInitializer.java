package com.techtip.communications.internal;

import com.techtip.communications.DialogsDeviation;
import com.techtip.communications.DialogsHandler;
import com.techtip.communications.DialogsIdentity;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

/**
 * Created by thennen on 2/5/16.
 */
public class DialogsChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final DialogsHandler handler;
    private final DialogsIdentity identity;
    private final boolean isServer;
    private DialogsNettyHandler nettyHandler;

    public DialogsChannelInitializer(DialogsHandler handler, DialogsIdentity identity, boolean isServer) {
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
        nettyHandler = new DialogsNettyHandler(handler, identity, isServer, ch.newPromise());
        ch.pipeline().addLast(nettyHandler);
    }

    /**
     * Wait for authentication to complete
     * @param timeoutMillis amount of millis to wait for authentication
     * @throws DialogsDeviation
     */
    public void awaitForAuth(long timeoutMillis) throws DialogsDeviation {
        nettyHandler.awaitForAuth(timeoutMillis);
    }
}
