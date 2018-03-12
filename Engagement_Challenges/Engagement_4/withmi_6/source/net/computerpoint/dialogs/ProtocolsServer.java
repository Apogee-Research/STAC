package net.computerpoint.dialogs;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.computerpoint.dialogs.internal.ProtocolsChannelInitializerBuilder;

/**
 * Waits for connections from a client
 */
public class ProtocolsServer {

    private int listenPort;
    private final ServerBootstrap bootstrap;
    private final EventLoopGroup serverGroup;

    public ProtocolsServer(int listenPort, ProtocolsManager manager, ProtocolsIdentity identity) {
        this(listenPort, manager, identity, new NioEventLoopGroup(1));
    }

    /**
     * @param listenPort port to listen on
     * @param manager used to notify users of data and connection events
     * @param identity the identiy of the server
     * @param eventLoopGroup
     */
    public ProtocolsServer(int listenPort, ProtocolsManager manager, ProtocolsIdentity identity, EventLoopGroup eventLoopGroup) {
        this.listenPort = listenPort;
        bootstrap = new ServerBootstrap();
        serverGroup = eventLoopGroup;
        bootstrap.group(serverGroup)
                .channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new ProtocolsChannelInitializerBuilder().assignConductor(manager).setIdentity(identity).fixIsServer(true).formProtocolsChannelInitializer());
    }

    /**
     * Starts serving asyncronously
     */
    public void serve() throws ProtocolsDeviation {
        try {
            bootstrap.bind(listenPort).sync();
        } catch (Exception e) {
            throw new ProtocolsDeviation(e);
        }
    }

    /**
     * Stops serving
     */
    public void close() {
        serverGroup.shutdownGracefully();
    }

}
