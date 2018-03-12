package com.techtip.communications;

import com.techtip.communications.internal.DialogsChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class DialogsClient {

    private final Bootstrap bootstrap;
    private EventLoopGroup group;
    private final DialogsChannelInitializer initializer;

    /**
     * @param dialogsHandler handler that will be called when data is available
     * @param identity the client's identity (what we present to the server)
     */
    public DialogsClient(DialogsHandler dialogsHandler, DialogsIdentity identity) {
        bootstrap = new Bootstrap();
        group = new NioEventLoopGroup(1);
        initializer = new DialogsChannelInitializer(dialogsHandler, identity, false);
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(initializer);
    }

    /**
     * Connects to a server
     * @param origin hostname to connect to
     * @param port the port to connect to
     * @return a CommsConnection to be used to send data
     * @throws DialogsDeviation
     */
    public DialogsConnection connect(String origin, int port) throws DialogsDeviation {
        try {
            Channel channel = bootstrap.connect(origin, port).sync().channel();
            initializer.awaitForAuth(10000);
            return channel.attr(DialogsConnection.CONNECTION_ATTR).get();
        } catch (Exception e) {
            // NOTE: if we don't catch the generic 'Exception' here then
            // some other sort of exception may wind up stalling us.
            // This is because Netty is doing some strange things.
            // See: https://github.com/netty/netty/issues/2597
            throw new DialogsDeviation(e);
        }
    }

    /**
     * Connects to a server at the specified address
     * @param addr the address of the server
     * @return the connection that can be used to talk to the server
     * @throws DialogsDeviation
     */
    public DialogsConnection connect(DialogsNetworkAddress addr) throws DialogsDeviation {
        return connect(addr.obtainOrigin(), addr.grabPort());
    }

    /**
     * Closes the client gracefully
     */
    public void close() {
        group.shutdownGracefully();
    }

    public EventLoopGroup fetchEventLoopGroup() {
        return group;
    }
}
