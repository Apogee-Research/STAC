package com.techtip.communications;

import com.techtip.communications.internal.DialogsChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Waits for connections from a client
 */
public class DialogsServer {

    private int listenPort;
    private final ServerBootstrap bootstrap;
    private final EventLoopGroup serverGroup;

    public DialogsServer(int listenPort, DialogsHandler handler, DialogsIdentity identity) {
        this(listenPort, handler, identity, new NioEventLoopGroup(1));
    }

    /**
     * @param listenPort port to listen on
     * @param handler used to notify users of data and connection events
     * @param identity the identiy of the server
     * @param eventLoopGroup
     */
    public DialogsServer(int listenPort, DialogsHandler handler, DialogsIdentity identity, EventLoopGroup eventLoopGroup) {
        this.listenPort = listenPort;
        bootstrap = new ServerBootstrap();
        serverGroup = eventLoopGroup;
        bootstrap.group(serverGroup)
                .channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new DialogsChannelInitializer(handler, identity, true));
    }

    /**
     * Starts serving asyncronously
     */
    public void serve() throws DialogsDeviation {
        try {
            bootstrap.bind(listenPort).sync();
        } catch (Exception e) {
            throw new DialogsDeviation(e);
        }
    }

    /**
     * Stops serving
     */
    public void close() {
        serverGroup.shutdownGracefully();
    }

}
