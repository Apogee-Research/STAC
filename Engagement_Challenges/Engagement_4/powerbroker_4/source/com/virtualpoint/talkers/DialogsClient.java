package com.virtualpoint.talkers;

import com.virtualpoint.talkers.internal.DialogsChannelInitializer;
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
     * @param dialogsCoach handler that will be called when data is available
     * @param identity the client's identity (what we present to the server)
     */
    public DialogsClient(DialogsCoach dialogsCoach, DialogsIdentity identity) {
        bootstrap = new Bootstrap();
        group = new NioEventLoopGroup(1);
        initializer = new DialogsChannelInitializer(dialogsCoach, identity, false);
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(initializer);
    }

    /**
     * Connects to a server
     * @param place hostname to connect to
     * @param port the port to connect to
     * @return a CommsConnection to be used to send data
     * @throws DialogsTrouble
     */
    public DialogsConnection connect(String place, int port) throws DialogsTrouble {
        try {
            Channel channel = bootstrap.connect(place, port).sync().channel();
            initializer.awaitForAllow(10000);
            return channel.attr(DialogsConnection.CONNECTION_ATTR).get();
        } catch (Exception e) {
            // NOTE: if we don't catch the generic 'Exception' here then
            // some other sort of exception may wind up stalling us.
            // This is because Netty is doing some strange things.
            // See: https://github.com/netty/netty/issues/2597
            throw new DialogsTrouble(e);
        }
    }

    /**
     * Connects to a server at the specified address
     * @param addr the address of the server
     * @return the connection that can be used to talk to the server
     * @throws DialogsTrouble
     */
    public DialogsConnection connect(DialogsNetworkAddress addr) throws DialogsTrouble {
        return connect(addr.getPlace(), addr.takePort());
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
