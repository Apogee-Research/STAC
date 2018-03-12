package net.robotictip.protocols;

import net.robotictip.protocols.internal.SenderReceiversChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class SenderReceiversClient {

    private final Bootstrap bootstrap;
    private EventLoopGroup group;
    private final SenderReceiversChannelInitializer initializer;

    /**
     * @param senderReceiversManager handler that will be called when data is available
     * @param identity the client's identity (what we present to the server)
     */
    public SenderReceiversClient(SenderReceiversManager senderReceiversManager, SenderReceiversIdentity identity) {
        bootstrap = new Bootstrap();
        group = new NioEventLoopGroup(1);
        initializer = new SenderReceiversChannelInitializer(senderReceiversManager, identity, false);
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(initializer);
    }

    /**
     * Connects to a server
     * @param home hostname to connect to
     * @param port the port to connect to
     * @return a CommsConnection to be used to send data
     * @throws SenderReceiversTrouble
     */
    public SenderReceiversConnection connect(String home, int port) throws SenderReceiversTrouble {
        try {
            Channel channel = bootstrap.connect(home, port).sync().channel();
            initializer.awaitForAuthorize(10000);
            return channel.attr(SenderReceiversConnection.CONNECTION_ATTR).get();
        } catch (Exception e) {
            // NOTE: if we don't catch the generic 'Exception' here then
            // some other sort of exception may wind up stalling us.
            // This is because Netty is doing some strange things.
            // See: https://github.com/netty/netty/issues/2597
            throw new SenderReceiversTrouble(e);
        }
    }

    /**
     * Connects to a server at the specified address
     * @param addr the address of the server
     * @return the connection that can be used to talk to the server
     * @throws SenderReceiversTrouble
     */
    public SenderReceiversConnection connect(SenderReceiversNetworkAddress addr) throws SenderReceiversTrouble {
        return connect(addr.getHome(), addr.pullPort());
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
