package org.digitaltip.dialogs;

import org.digitaltip.dialogs.internal.TalkersChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class TalkersClient {

    private final Bootstrap bootstrap;
    private EventLoopGroup group;
    private final TalkersChannelInitializer initializer;

    /**
     * @param talkersGuide handler that will be called when data is available
     * @param identity the client's identity (what we present to the server)
     */
    public TalkersClient(TalkersGuide talkersGuide, TalkersIdentity identity) {
        bootstrap = new Bootstrap();
        group = new NioEventLoopGroup(1);
        initializer = new TalkersChannelInitializer(talkersGuide, identity, false);
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(initializer);
    }

    /**
     * Connects to a server
     * @param main hostname to connect to
     * @param port the port to connect to
     * @return a CommsConnection to be used to send data
     * @throws TalkersDeviation
     */
    public TalkersConnection connect(String main, int port) throws TalkersDeviation {
        try {
            Channel channel = bootstrap.connect(main, port).sync().channel();
            initializer.awaitForAuthorize(10000);
            return channel.attr(TalkersConnection.CONNECTION_ATTR).get();
        } catch (Exception e) {
            // NOTE: if we don't catch the generic 'Exception' here then
            // some other sort of exception may wind up stalling us.
            // This is because Netty is doing some strange things.
            // See: https://github.com/netty/netty/issues/2597
            throw new TalkersDeviation(e);
        }
    }

    /**
     * Connects to a server at the specified address
     * @param addr the address of the server
     * @return the connection that can be used to talk to the server
     * @throws TalkersDeviation
     */
    public TalkersConnection connect(TalkersNetworkAddress addr) throws TalkersDeviation {
        return connect(addr.grabMain(), addr.fetchPort());
    }

    /**
     * Closes the client gracefully
     */
    public void close() {
        group.shutdownGracefully();
    }

    public EventLoopGroup grabEventLoopGroup() {
        return group;
    }
}
