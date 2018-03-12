package net.roboticapex.senderReceivers;

import net.roboticapex.senderReceivers.internal.SenderReceiversChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Waits for connections from a client
 */
public class SenderReceiversServer {

    private int listenPort;
    private final ServerBootstrap bootstrap;
    private final EventLoopGroup serverGroup;

    public SenderReceiversServer(int listenPort, SenderReceiversHandler handler, SenderReceiversIdentity identity) {
        this(listenPort, handler, identity, new NioEventLoopGroup(1));
    }

    /**
     * @param listenPort port to listen on
     * @param handler used to notify users of data and connection events
     * @param identity the identiy of the server
     * @param eventLoopGroup
     */
    public SenderReceiversServer(int listenPort, SenderReceiversHandler handler, SenderReceiversIdentity identity, EventLoopGroup eventLoopGroup) {
        this.listenPort = listenPort;
        bootstrap = new ServerBootstrap();
        serverGroup = eventLoopGroup;
        bootstrap.group(serverGroup)
                .channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new SenderReceiversChannelInitializer(handler, identity, true));
    }

    /**
     * Starts serving asyncronously
     */
    public void serve() throws SenderReceiversDeviation {
        try {
            bootstrap.bind(listenPort).sync();
        } catch (Exception e) {
            throw new SenderReceiversDeviation(e);
        }
    }

    /**
     * Stops serving
     */
    public void close() {
        serverGroup.shutdownGracefully();
    }

}
