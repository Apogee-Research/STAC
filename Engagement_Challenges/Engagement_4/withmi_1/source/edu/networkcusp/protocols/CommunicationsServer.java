package edu.networkcusp.protocols;

import edu.networkcusp.protocols.internal.CommunicationsChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Waits for connections from a client
 */
public class CommunicationsServer {

    private int listenPort;
    private final ServerBootstrap bootstrap;
    private final EventLoopGroup serverGroup;

    public CommunicationsServer(int listenPort, CommunicationsGuide guide, CommunicationsIdentity identity) {
        this(listenPort, guide, identity, new NioEventLoopGroup(1));
    }

    /**
     * @param listenPort port to listen on
     * @param guide used to notify users of data and connection events
     * @param identity the identiy of the server
     * @param eventLoopGroup
     */
    public CommunicationsServer(int listenPort, CommunicationsGuide guide, CommunicationsIdentity identity, EventLoopGroup eventLoopGroup) {
        this.listenPort = listenPort;
        bootstrap = new ServerBootstrap();
        serverGroup = eventLoopGroup;
        bootstrap.group(serverGroup)
                .channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new CommunicationsChannelInitializer(guide, identity, true));
    }

    /**
     * Starts serving asyncronously
     */
    public void serve() throws CommunicationsFailure {
        try {
            bootstrap.bind(listenPort).sync();
        } catch (Exception e) {
            throw new CommunicationsFailure(e);
        }
    }

    /**
     * Stops serving
     */
    public void close() {
        serverGroup.shutdownGracefully();
    }

}
