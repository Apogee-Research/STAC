package edu.computerapex.dialogs;

import edu.computerapex.dialogs.internal.CommunicationsChannelInitializer;
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

    public CommunicationsServer(int listenPort, CommunicationsHandler handler, CommunicationsIdentity identity) {
        this(listenPort, handler, identity, new NioEventLoopGroup(1));
    }

    /**
     * @param listenPort port to listen on
     * @param handler used to notify users of data and connection events
     * @param identity the identiy of the server
     * @param eventLoopGroup
     */
    public CommunicationsServer(int listenPort, CommunicationsHandler handler, CommunicationsIdentity identity, EventLoopGroup eventLoopGroup) {
        this.listenPort = listenPort;
        bootstrap = new ServerBootstrap();
        serverGroup = eventLoopGroup;
        bootstrap.group(serverGroup)
                .channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new CommunicationsChannelInitializer(handler, identity, true));
    }

    /**
     * Starts serving asyncronously
     */
    public void serve() throws CommunicationsDeviation {
        try {
            bootstrap.bind(listenPort).sync();
        } catch (Exception e) {
            throw new CommunicationsDeviation(e);
        }
    }

    /**
     * Stops serving
     */
    public void close() {
        serverGroup.shutdownGracefully();
    }

}
