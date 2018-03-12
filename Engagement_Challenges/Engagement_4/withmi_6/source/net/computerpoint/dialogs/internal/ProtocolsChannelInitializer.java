package net.computerpoint.dialogs.internal;

import net.computerpoint.dialogs.ProtocolsDeviation;
import net.computerpoint.dialogs.ProtocolsManager;
import net.computerpoint.dialogs.ProtocolsIdentity;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

/**
 * Created by thennen on 2/5/16.
 */
public class ProtocolsChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final ProtocolsManager manager;
    private final ProtocolsIdentity identity;
    private final boolean isServer;
    private ProtocolsNettyManager nettyManager;

    public ProtocolsChannelInitializer(ProtocolsManager conductor, ProtocolsIdentity identity, boolean isServer) {
        this.manager = conductor;
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
        nettyManager = new ProtocolsNettyManager(manager, identity, isServer, ch.newPromise());
        ch.pipeline().addLast(nettyManager);
    }

    /**
     * Wait for authentication to complete
     * @param timeoutMillis amount of millis to wait for authentication
     * @throws ProtocolsDeviation
     */
    public void awaitForAuthorize(long timeoutMillis) throws ProtocolsDeviation {
        nettyManager.awaitForAuthorize(timeoutMillis);
    }
}
