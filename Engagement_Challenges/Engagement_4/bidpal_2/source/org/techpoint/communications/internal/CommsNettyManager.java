package org.techpoint.communications.internal;

import org.techpoint.communications.CommsConnection;
import org.techpoint.communications.CommsRaiser;
import org.techpoint.communications.CommsManager;
import org.techpoint.communications.CommsIdentity;
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
public class CommsNettyManager extends ChannelDuplexHandler { //extends SimpleChannelInboundHandler<byte[]> {
    private final CommsManager manager;
    private final CommsCryptoState cryptoState;
    private final boolean isServer;
    private Promise authenticatedPromise;

    /**
     *
     * @param coordinator the CommsHandler that will be notified when data arrives or connections close or open
     * @param identity the identity of the entity using this handler (i.e. this end of the connection)
     * @param isServer true if this is being used as a server
     * @param authenticatedPromise signaled when we're successfully authenticated
     * @throws CommsRaiser
     */
    public CommsNettyManager(CommsManager coordinator, CommsIdentity identity, boolean isServer, Promise authenticatedPromise) throws CommsRaiser {
        this.manager = coordinator;
        this.cryptoState = new CommsCryptoState(identity);
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
            channelActiveHerder(ctx);
        }
    }

    private void channelActiveHerder(ChannelHandlerContext ctx) throws CommsRaiser, InvalidParameterSpecException, InvalidKeyException {
        byte[] setupMsg = cryptoState.fetchNextSetupMessage();
        ctx.writeAndFlush(setupMsg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        // TCP connection was just closed
        CommsConnection connection = ctx.channel().attr(CommsConnection.CONNECTION_ATTR).get();
        // the connection may be null, if we did not successfully connect to the other user
        // if that's the case, we don't need to disconnect from the other user
        if (connection != null) {
            manager.closedConnection(connection);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (cryptoState.isReady()) {
            // If the cryptoState is ready, then this is just regular user data, give it to them
            // after we decrypt it
            channelReadHelp(ctx, (byte[]) msg);
        } else {
            // if it isn't ready, the this should be a setup message, process it as one
            cryptoState.processNextSetupMessage((byte[]) msg);

            // does it have another setup message to send?
            if (cryptoState.hasSetupMessage()) {
                byte[] nextMsg = cryptoState.fetchNextSetupMessage();
                ctx.writeAndFlush(nextMsg);
            }


            // has the RSA authentication test failed?
            if (cryptoState.hasFailed()) {
                // if it has, close the connection
                new CommsNettyManagerHerder(ctx).invoke();
            }

            // is it ready now (are we all authenticated and everything)?
            if (cryptoState.isReady()) {
                Channel ch = ctx.channel();
                CommsConnection connection = new CommsConnection(ch, cryptoState.fetchTheirIdentity());
                ch.attr(CommsConnection.CONNECTION_ATTR).set(connection);

                // clients will be waiting for this event
                authenticatedPromise.setSuccess(null);

                // the server will want to know about the new connection
                // we don't even notify it until authentication is complete
                if (isServer) {
                    channelReadCoordinator(connection);
                }
            }
        }
    }

    private void channelReadCoordinator(CommsConnection connection) throws CommsRaiser {
        manager.newConnection(connection);
    }

    private void channelReadHelp(ChannelHandlerContext ctx, byte[] msg) throws CommsRaiser, InvalidAlgorithmParameterException, InvalidKeyException {
        byte[] data = cryptoState.decrypt(msg);

        manager.handle(ctx.channel().attr(CommsConnection.CONNECTION_ATTR).get(), data);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (cryptoState.isReady()) {
            // we can send user data now, we just have to encrypt it first...
            writeExecutor(ctx, (byte[]) msg, promise);
        } else {
            throw new CommsRaiser("Trying to send data, but cryptostate isn't ready yet!");
        }
    }

    private void writeExecutor(ChannelHandlerContext ctx, byte[] msg, ChannelPromise promise) throws Exception {
        byte[] data = cryptoState.encrypt(msg);
        super.write(ctx, data, promise);
    }

    /**
     * Wait for authentication to be completed
     * @param timeoutmillis the amount of time to wait...
     * @throws CommsRaiser
     */
    public void awaitForAuth(long timeoutmillis) throws CommsRaiser {
        try {
            authenticatedPromise.await(timeoutmillis);
            if (!authenticatedPromise.isSuccess()) {
                throw new CommsRaiser(authenticatedPromise.cause().getMessage());
            }
        } catch (InterruptedException e) {
            throw new CommsRaiser(e);
        }
    }

    private class CommsNettyManagerHerder {
        private ChannelHandlerContext ctx;

        public CommsNettyManagerHerder(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        public void invoke() {
            ctx.close();
            authenticatedPromise.setFailure(new CommsRaiser("Failed handshake"));
        }
    }
}
