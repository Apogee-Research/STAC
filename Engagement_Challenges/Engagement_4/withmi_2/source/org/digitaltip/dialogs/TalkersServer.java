package org.digitaltip.dialogs;

import org.digitaltip.dialogs.internal.TalkersChannelInitializer;
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

    public TalkersServer(int listenPort, TalkersGuide guide, TalkersIdentity identity) {
        this(listenPort, guide, identity, new NioEventLoopGroup(1));
    }

    /**
     * @param listenPort port to listen on
     * @param guide used to notify users of data and connection events
     * @param identity the identiy of the server
     * @param eventLoopGroup
     */
    public TalkersServer(int listenPort, TalkersGuide guide, TalkersIdentity identity, EventLoopGroup eventLoopGroup) {
        this.listenPort = listenPort;
        bootstrap = new ServerBootstrap();
        serverGroup = eventLoopGroup;
        bootstrap.group(serverGroup)
                .channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new TalkersChannelInitializer(guide, identity, true));
    }

    /**
     * Starts serving asyncronously
     */
    public void serve() throws TalkersDeviation {
        try {
            bootstrap.bind(listenPort).sync();
        } catch (Exception e) {
            throw new TalkersDeviation(e);
        }
    }

    /**
     * Stops serving
     */
    public void close() {
        serverGroup.shutdownGracefully();
    }

}
