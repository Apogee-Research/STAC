package com.digitalpoint.dialogs.internal;

import com.digitalpoint.dialogs.SenderReceiversConnection;
import com.digitalpoint.dialogs.SenderReceiversException;
import com.digitalpoint.dialogs.SenderReceiversCoach;
import com.digitalpoint.dialogs.SenderReceiversIdentity;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.Promise;

import java.security.InvalidKeyException;
import java.security.spec.InvalidParameterSpecException;


/**
 * Used in the Netty framework to do our auth and crypto
 */
public class SenderReceiversNettyCoach extends ChannelDuplexHandler { //extends SimpleChannelInboundHandler<byte[]> {
    private final SenderReceiversCoach coach;
    private final SenderReceiversCryptoState cryptoState;
    private final boolean isServer;
    private Promise authenticatedPromise;

    /**
     *
     * @param coach the CommsHandler that will be notified when data arrives or connections close or open
     * @param identity the identity of the entity using this handler (i.e. this end of the connection)
     * @param isServer true if this is being used as a server
     * @param authenticatedPromise signaled when we're successfully authenticated
     * @throws SenderReceiversException
     */
    public SenderReceiversNettyCoach(SenderReceiversCoach coach, SenderReceiversIdentity identity, boolean isServer, Promise authenticatedPromise) throws SenderReceiversException {
        this.coach = coach;
        this.cryptoState = new SenderReceiversCryptoState(identity);
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
        SenderReceiversConnection connection = ctx.channel().attr(SenderReceiversConnection.CONNECTION_ATTR).get();
        // the connection may be null, if we did not successfully connect to the other user
        // if that's the case, we don't need to disconnect from the other user
        if (connection != null) {
            channelInactiveUtility(connection);
        }
    }

    private void channelInactiveUtility(SenderReceiversConnection connection) throws SenderReceiversException {
        coach.closedConnection(connection);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (cryptoState.isReady()) {
            // If the cryptoState is ready, then this is just regular user data, give it to them
            // after we decrypt it
            byte[] data = cryptoState.decrypt((byte[]) msg);

            coach.handle(ctx.channel().attr(SenderReceiversConnection.CONNECTION_ATTR).get(), data);
        } else {
            // if it isn't ready, the this should be a setup message, process it as one
            cryptoState.processNextSetupMessage((byte[]) msg);

            // does it have another setup message to send?
            if (cryptoState.hasSetupMessage()) {
                new SenderReceiversNettyCoachEntity(ctx).invoke();
            }


            // has the RSA authentication test failed?
            if (cryptoState.hasFailed()) {
                // if it has, close the connection
                channelReadExecutor(ctx);
            }

            // is it ready now (are we all authenticated and everything)?
            if (cryptoState.isReady()) {
                Channel ch = ctx.channel();
                SenderReceiversConnection connection = new SenderReceiversConnection(ch, cryptoState.grabTheirIdentity());
                ch.attr(SenderReceiversConnection.CONNECTION_ATTR).set(connection);

                // clients will be waiting for this event
                authenticatedPromise.setSuccess(null);

                // the server will want to know about the new connection
                // we don't even notify it until authentication is complete
                if (isServer) {
                    channelReadGuide(connection);
                }
            }
        }
    }

    private void channelReadGuide(SenderReceiversConnection connection) throws SenderReceiversException {
        coach.newConnection(connection);
    }

    private void channelReadExecutor(ChannelHandlerContext ctx) {
        ctx.close();
        authenticatedPromise.setFailure(new SenderReceiversException("Failed handshake"));
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (cryptoState.isReady()) {
            // we can send user data now, we just have to encrypt it first...
            writeWorker(ctx, (byte[]) msg, promise);
        } else {
            throw new SenderReceiversException("Trying to send data, but cryptostate isn't ready yet!");
        }
    }

    private void writeWorker(ChannelHandlerContext ctx, byte[] msg, ChannelPromise promise) throws Exception {
        byte[] data = cryptoState.encrypt(msg);
        super.write(ctx, data, promise);
    }

    /**
     * Wait for authentication to be completed
     * @param timeoutmillis the amount of time to wait...
     * @throws SenderReceiversException
     */
    public void awaitForAuthorize(long timeoutmillis) throws SenderReceiversException {
        try {
            authenticatedPromise.await(timeoutmillis);
            if (!authenticatedPromise.isSuccess()) {
                awaitForAuthorizeUtility();
            }
        } catch (InterruptedException e) {
            throw new SenderReceiversException(e);
        }
    }

    private void awaitForAuthorizeUtility() throws SenderReceiversException {
        throw new SenderReceiversException(authenticatedPromise.cause().getMessage());
    }

    private class SenderReceiversNettyCoachEntity {
        private ChannelHandlerContext ctx;

        public SenderReceiversNettyCoachEntity(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        public void invoke() throws SenderReceiversException, InvalidParameterSpecException, InvalidKeyException {
            byte[] nextMsg = cryptoState.pullNextSetupMessage();
            ctx.writeAndFlush(nextMsg);
        }
    }
}
