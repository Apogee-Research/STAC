package edu.networkcusp.senderReceivers;

import edu.networkcusp.senderReceivers.internal.ProtocolsChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Waits for connections from a client
 */
public class ProtocolsServer {

    private int listenPort;
    private final ServerBootstrap bootstrap;
    private final EventLoopGroup serverGroup;

    public ProtocolsServer(int listenPort, ProtocolsHandler handler, ProtocolsIdentity identity) {
        this(listenPort, handler, identity, new NioEventLoopGroup(1));
    }

    /**
     * @param listenPort port to listen on
     * @param handler used to notify users of data and connection events
     * @param identity the identiy of the server
     * @param eventLoopGroup
     */
    public ProtocolsServer(int listenPort, ProtocolsHandler handler, ProtocolsIdentity identity, EventLoopGroup eventLoopGroup) {
        this.listenPort = listenPort;
        bootstrap = new ServerBootstrap();
        serverGroup = eventLoopGroup;
        bootstrap.group(serverGroup)
                .channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new ProtocolsChannelInitializer(handler, identity, true));
    }

    /**
     * Starts serving asyncronously
     */
    public void serve() throws ProtocolsRaiser {
        try {
            bootstrap.bind(listenPort).sync();
        } catch (Exception e) {
            throw new ProtocolsRaiser(e);
        }
    }

    /**
     * Stops serving
     */
    public void close() {
        serverGroup.shutdownGracefully();
    }

}
