package org.digitalapex.talkers.internal;

import org.digitalapex.talkers.TalkersRaiser;
import org.digitalapex.talkers.TalkersCoach;
import org.digitalapex.talkers.TalkersIdentity;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

/**
 * Created by thennen on 2/5/16.
 */
public class TalkersChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final TalkersCoach coach;
    private final TalkersIdentity identity;
    private final boolean isServer;
    private TalkersNettyCoach nettyCoach;

    public TalkersChannelInitializer(TalkersCoach coach, TalkersIdentity identity, boolean isServer) {
        this.coach = coach;
        this.identity = identity;
        this.isServer = isServer;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        // using the frame encoder and decoder should make it safe
        // to assume a 'read' will get the entire message...
        ch.pipeline()
                // TODO: allow the user to figure out what the max frame size is...
                .addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 4))
                .addLast("frameEncoder", new LengthFieldPrepender(4))
                .addLast("bytesEncoder", new ByteArrayEncoder())
                .addLast("bytesDecoder", new ByteArrayDecoder());

        // this is what does the authentication and encryption
        nettyCoach = new TalkersNettyCoach(coach, identity, isServer, ch.newPromise());
        ch.pipeline().addLast(nettyCoach);
    }

    /**
     * Wait for authentication to complete
     * @param timeoutMillis amount of millis to wait for authentication
     * @throws TalkersRaiser
     */
    public void awaitForAuth(long timeoutMillis) throws TalkersRaiser {
        nettyCoach.awaitForAuth(timeoutMillis);
    }
}
