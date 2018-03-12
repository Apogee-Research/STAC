package org.digitalapex.talkers;

import org.digitalapex.talkers.internal.TalkersChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Waits for connections from a client
 */
public class TalkersServer {

    private int listenPort;
    private final ServerBootstrap bootstrap;
    private final EventLoopGroup serverGroup;

    public TalkersServer(int listenPort, TalkersCoach coach, TalkersIdentity identity) {
        this(listenPort, coach, identity, new NioEventLoopGroup(1));
    }

    /**
     * @param listenPort port to listen on
     * @param coach used to notify users of data and connection events
     * @param identity the identiy of the server
     * @param eventLoopGroup
     */
    public TalkersServer(int listenPort, TalkersCoach coach, TalkersIdentity identity, EventLoopGroup eventLoopGroup) {
        this.listenPort = listenPort;
        bootstrap = new ServerBootstrap();
        serverGroup = eventLoopGroup;
        bootstrap.group(serverGroup)
                .channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new TalkersChannelInitializer(coach, identity, true));
    }

    /**
     * Starts serving asyncronously
     */
    public void serve() throws TalkersRaiser {
        try {
            bootstrap.bind(listenPort).sync();
        } catch (Exception e) {
            throw new TalkersRaiser(e);
        }
    }

    /**
     * Stops serving
     */
    public void close() {
        serverGroup.shutdownGracefully();
    }

}
