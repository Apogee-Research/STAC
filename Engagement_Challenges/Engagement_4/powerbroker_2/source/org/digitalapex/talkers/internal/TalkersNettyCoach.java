package org.digitalapex.talkers.internal;

import org.digitalapex.talkers.TalkersConnection;
import org.digitalapex.talkers.TalkersRaiser;
import org.digitalapex.talkers.TalkersCoach;
import org.digitalapex.talkers.TalkersIdentity;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.Promise;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidParameterSpecException;


/**
 * Used in the Netty framework to do our auth and crypto
 */
public class TalkersNettyCoach extends ChannelDuplexHandler { //extends SimpleChannelInboundHandler<byte[]> {
    private final TalkersCoach coach;
    private final TalkersCryptoState cryptoState;
    private final boolean isServer;
    private Promise authenticatedPromise;

    /**
     *
     * @param coach the CommsHandler that will be notified when data arrives or connections close or open
     * @param identity the identity of the entity using this handler (i.e. this end of the connection)
     * @param isServer true if this is being used as a server
     * @param authenticatedPromise signaled when we're successfully authenticated
     * @throws TalkersRaiser
     */
    public TalkersNettyCoach(TalkersCoach coach, TalkersIdentity identity, boolean isServer, Promise authenticatedPromise) throws TalkersRaiser {
        this.coach = coach;
        this.cryptoState = new TalkersCryptoState(identity);
        this.isServer = isServer;
        this.authenticatedPromise = authenticatedPromise;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        // TCP connection was just established, time to deal with
        // authenication, but client goes first...
        if (!isServer && cryptoState.hasSetupMessage()) {
            // client is responsible for sending first message
            byte[] setupMsg = cryptoState.pullNextSetupMessage();
            ctx.writeAndFlush(setupMsg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        // TCP connection was just closed
        TalkersConnection connection = ctx.channel().attr(TalkersConnection.CONNECTION_ATTR).get();
        // the connection may be null, if we did not successfully connect to the other user
        // if that's the case, we don't need to disconnect from the other user
        if (connection != null) {
            coach.closedConnection(connection);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (cryptoState.isReady()) {
            // If the cryptoState is ready, then this is just regular user data, give it to them
            // after we decrypt it
            channelReadEntity(ctx, (byte[]) msg);
        } else {
            // if it isn't ready, the this should be a setup message, process it as one
            channelReadSupervisor(ctx, (byte[]) msg);
        }
    }

    private void channelReadSupervisor(ChannelHandlerContext ctx, byte[] msg) throws TalkersRaiser, InvalidParameterSpecException, InvalidKeyException {
        cryptoState.processNextSetupMessage(msg);

        // does it have another setup message to send?
        if (cryptoState.hasSetupMessage()) {
            channelReadSupervisorTarget(ctx);
        }


        // has the RSA authentication test failed?
        if (cryptoState.hasFailed()) {
            // if it has, close the connection
            ctx.close();
            authenticatedPromise.setFailure(new TalkersRaiser("Failed handshake"));
        }

        // is it ready now (are we all authenticated and everything)?
        if (cryptoState.isReady()) {
            Channel ch = ctx.channel();
            TalkersConnection connection = new TalkersConnection(ch, cryptoState.obtainTheirIdentity());
            ch.attr(TalkersConnection.CONNECTION_ATTR).set(connection);

            // clients will be waiting for this event
            authenticatedPromise.setSuccess(null);

            // the server will want to know about the new connection
            // we don't even notify it until authentication is complete
            if (isServer) {
                coach.newConnection(connection);
            }
        }
    }

    private void channelReadSupervisorTarget(ChannelHandlerContext ctx) throws TalkersRaiser, InvalidParameterSpecException, InvalidKeyException {
        byte[] nextMsg = cryptoState.pullNextSetupMessage();
        ctx.writeAndFlush(nextMsg);
    }

    private void channelReadEntity(ChannelHandlerContext ctx, byte[] msg) throws TalkersRaiser, InvalidAlgorithmParameterException, InvalidKeyException {
        byte[] data = cryptoState.decrypt(msg);

        coach.handle(ctx.channel().attr(TalkersConnection.CONNECTION_ATTR).get(), data);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (cryptoState.isReady()) {
            // we can send user data now, we just have to encrypt it first...
            writeHome(ctx, (byte[]) msg, promise);
        } else {
            throw new TalkersRaiser("Trying to send data, but cryptostate isn't ready yet!");
        }
    }

    private void writeHome(ChannelHandlerContext ctx, byte[] msg, ChannelPromise promise) throws Exception {
        byte[] data = cryptoState.encrypt(msg);
        super.write(ctx, data, promise);
    }

    /**
     * Wait for authentication to be completed
     * @param timeoutmillis the amount of time to wait...
     * @throws TalkersRaiser
     */
    public void awaitForAuth(long timeoutmillis) throws TalkersRaiser {
        try {
            authenticatedPromise.await(timeoutmillis);
            if (!authenticatedPromise.isSuccess()) {
                awaitForAuthHome();
            }
        } catch (InterruptedException e) {
            throw new TalkersRaiser(e);
        }
    }

    private void awaitForAuthHome() throws TalkersRaiser {
        throw new TalkersRaiser(authenticatedPromise.cause().getMessage());
    }
}
