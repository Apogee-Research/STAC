package org.digitaltip.dialogs.internal;

import org.digitaltip.dialogs.TalkersConnection;
import org.digitaltip.dialogs.TalkersDeviation;
import org.digitaltip.dialogs.TalkersGuide;
import org.digitaltip.dialogs.TalkersIdentity;
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
public class TalkersNettyGuide extends ChannelDuplexHandler { //extends SimpleChannelInboundHandler<byte[]> {
    private final TalkersGuide guide;
    private final TalkersCryptoState cryptoState;
    private final boolean isServer;
    private Promise authenticatedPromise;

    /**
     *
     * @param guide the CommsHandler that will be notified when data arrives or connections close or open
     * @param identity the identity of the entity using this handler (i.e. this end of the connection)
     * @param isServer true if this is being used as a server
     * @param authenticatedPromise signaled when we're successfully authenticated
     * @throws TalkersDeviation
     */
    public TalkersNettyGuide(TalkersGuide guide, TalkersIdentity identity, boolean isServer, Promise authenticatedPromise) throws TalkersDeviation {
        this.guide = guide;
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
            byte[] setupMsg = cryptoState.grabNextSetupMessage();
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
            channelInactiveWorker(connection);
        }
    }

    private void channelInactiveWorker(TalkersConnection connection) throws TalkersDeviation {
        guide.closedConnection(connection);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (cryptoState.isReady()) {
            // If the cryptoState is ready, then this is just regular user data, give it to them
            // after we decrypt it
            channelReadService(ctx, (byte[]) msg);
        } else {
            // if it isn't ready, the this should be a setup message, process it as one
            channelReadCoordinator(ctx, (byte[]) msg);
        }
    }

    private void channelReadCoordinator(ChannelHandlerContext ctx, byte[] msg) throws TalkersDeviation, InvalidParameterSpecException, InvalidKeyException {
        cryptoState.processNextSetupMessage(msg);

        // does it have another setup message to send?
        if (cryptoState.hasSetupMessage()) {
            byte[] nextMsg = cryptoState.grabNextSetupMessage();
            ctx.writeAndFlush(nextMsg);
        }


        // has the RSA authentication test failed?
        if (cryptoState.hasFailed()) {
            // if it has, close the connection
            channelReadCoordinatorHelper(ctx);
        }

        // is it ready now (are we all authenticated and everything)?
        if (cryptoState.isReady()) {
            Channel ch = ctx.channel();
            TalkersConnection connection = new TalkersConnection(ch, cryptoState.grabTheirIdentity());
            ch.attr(TalkersConnection.CONNECTION_ATTR).set(connection);

            // clients will be waiting for this event
            authenticatedPromise.setSuccess(null);

            // the server will want to know about the new connection
            // we don't even notify it until authentication is complete
            if (isServer) {
                guide.newConnection(connection);
            }
        }
    }

    private void channelReadCoordinatorHelper(ChannelHandlerContext ctx) {
        ctx.close();
        authenticatedPromise.setFailure(new TalkersDeviation("Failed handshake"));
    }

    private void channelReadService(ChannelHandlerContext ctx, byte[] msg) throws TalkersDeviation, InvalidAlgorithmParameterException, InvalidKeyException {
        byte[] data = cryptoState.decrypt(msg);

        guide.handle(ctx.channel().attr(TalkersConnection.CONNECTION_ATTR).get(), data);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (cryptoState.isReady()) {
            // we can send user data now, we just have to encrypt it first...
            byte[] data = cryptoState.encrypt((byte[]) msg);
            super.write(ctx, data, promise);
        } else {
            writeHome();
        }
    }

    private void writeHome() throws TalkersDeviation {
        throw new TalkersDeviation("Trying to send data, but cryptostate isn't ready yet!");
    }

    /**
     * Wait for authentication to be completed
     * @param timeoutmillis the amount of time to wait...
     * @throws TalkersDeviation
     */
    public void awaitForAuthorize(long timeoutmillis) throws TalkersDeviation {
        try {
            authenticatedPromise.await(timeoutmillis);
            if (!authenticatedPromise.isSuccess()) {
                new TalkersNettyGuideAssist().invoke();
            }
        } catch (InterruptedException e) {
            throw new TalkersDeviation(e);
        }
    }

    private class TalkersNettyGuideAssist {
        public void invoke() throws TalkersDeviation {
            throw new TalkersDeviation(authenticatedPromise.cause().getMessage());
        }
    }
}
