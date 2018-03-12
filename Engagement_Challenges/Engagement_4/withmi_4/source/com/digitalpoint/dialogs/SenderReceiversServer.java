package com.digitalpoint.dialogs;

import com.digitalpoint.dialogs.internal.SenderReceiversChannelInitializerBuilder;
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

    public SenderReceiversServer(int listenPort, SenderReceiversCoach coach, SenderReceiversIdentity identity) {
        this(listenPort, coach, identity, new NioEventLoopGroup(1));
    }

    /**
     * @param listenPort port to listen on
     * @param coach used to notify users of data and connection events
     * @param identity the identiy of the server
     * @param eventLoopGroup
     */
    public SenderReceiversServer(int listenPort, SenderReceiversCoach coach, SenderReceiversIdentity identity, EventLoopGroup eventLoopGroup) {
        this.listenPort = listenPort;
        bootstrap = new ServerBootstrap();
        serverGroup = eventLoopGroup;
        bootstrap.group(serverGroup)
                .channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new SenderReceiversChannelInitializerBuilder().fixCoach(coach).assignIdentity(identity).fixIsServer(true).makeSenderReceiversChannelInitializer());
    }

    /**
     * Starts serving asyncronously
     */
    public void serve() throws SenderReceiversException {
        try {
            bootstrap.bind(listenPort).sync();
        } catch (Exception e) {
            throw new SenderReceiversException(e);
        }
    }

    /**
     * Stops serving
     */
    public void close() {
        serverGroup.shutdownGracefully();
    }

}
