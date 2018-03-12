package com.virtualpoint.talkers;

import com.virtualpoint.talkers.internal.DialogsChannelInitializer;
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

    public DialogsServer(int listenPort, DialogsCoach coach, DialogsIdentity identity) {
        this(listenPort, coach, identity, new NioEventLoopGroup(1));
    }

    /**
     * @param listenPort port to listen on
     * @param coach used to notify users of data and connection events
     * @param identity the identiy of the server
     * @param eventLoopGroup
     */
    public DialogsServer(int listenPort, DialogsCoach coach, DialogsIdentity identity, EventLoopGroup eventLoopGroup) {
        this.listenPort = listenPort;
        bootstrap = new ServerBootstrap();
        serverGroup = eventLoopGroup;
        bootstrap.group(serverGroup)
                .channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new DialogsChannelInitializer(coach, identity, true));
    }

    /**
     * Starts serving asyncronously
     */
    public void serve() throws DialogsTrouble {
        try {
            bootstrap.bind(listenPort).sync();
        } catch (Exception e) {
            throw new DialogsTrouble(e);
        }
    }

    /**
     * Stops serving
     */
    public void close() {
        serverGroup.shutdownGracefully();
    }

}
