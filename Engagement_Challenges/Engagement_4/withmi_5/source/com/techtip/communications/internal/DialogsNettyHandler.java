package com.techtip.communications.internal;

import com.techtip.communications.DialogsConnection;
import com.techtip.communications.DialogsDeviation;
import com.techtip.communications.DialogsHandler;
import com.techtip.communications.DialogsIdentity;
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
public class DialogsNettyHandler extends ChannelDuplexHandler { //extends SimpleChannelInboundHandler<byte[]> {
    private final DialogsHandler handler;
    private final DialogsCryptoState cryptoState;
    private final boolean isServer;
    private Promise authenticatedPromise;

    /**
     *
     * @param handler the CommsHandler that will be notified when data arrives or connections close or open
     * @param identity the identity of the entity using this handler (i.e. this end of the connection)
     * @param isServer true if this is being used as a server
     * @param authenticatedPromise signaled when we're successfully authenticated
     * @throws DialogsDeviation
     */
    public DialogsNettyHandler(DialogsHandler handler, DialogsIdentity identity, boolean isServer, Promise authenticatedPromise) throws DialogsDeviation {
        this.handler = handler;
        this.cryptoState = new DialogsCryptoState(identity);
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
            channelActiveSupervisor(ctx);
        }
    }

    private void channelActiveSupervisor(ChannelHandlerContext ctx) throws DialogsDeviation, InvalidParameterSpecException, InvalidKeyException {
        byte[] setupMsg = cryptoState.getNextSetupMessage();
        ctx.writeAndFlush(setupMsg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        // TCP connection was just closed
        DialogsConnection connection = ctx.channel().attr(DialogsConnection.CONNECTION_ATTR).get();
        // the connection may be null, if we did not successfully connect to the other user
        // if that's the case, we don't need to disconnect from the other user
        if (connection != null) {
            handler.closedConnection(connection);
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
            channelReadGateKeeper(ctx, (byte[]) msg);
        }
    }

    private void channelReadGateKeeper(ChannelHandlerContext ctx, byte[] msg) throws DialogsDeviation, InvalidParameterSpecException, InvalidKeyException {
        cryptoState.processNextSetupMessage(msg);

        // does it have another setup message to send?
        if (cryptoState.hasSetupMessage()) {
            channelReadGateKeeperExecutor(ctx);
        }


        // has the RSA authentication test failed?
        if (cryptoState.hasFailed()) {
            // if it has, close the connection
            channelReadGateKeeperEngine(ctx);
        }

        // is it ready now (are we all authenticated and everything)?
        if (cryptoState.isReady()) {
            Channel ch = ctx.channel();
            DialogsConnection connection = new DialogsConnection(ch, cryptoState.takeTheirIdentity());
            ch.attr(DialogsConnection.CONNECTION_ATTR).set(connection);

            // clients will be waiting for this event
            authenticatedPromise.setSuccess(null);

            // the server will want to know about the new connection
            // we don't even notify it until authentication is complete
            if (isServer) {
                handler.newConnection(connection);
            }
        }
    }

    private void channelReadGateKeeperEngine(ChannelHandlerContext ctx) {
        new DialogsNettyHandlerEntity(ctx).invoke();
    }

    private void channelReadGateKeeperExecutor(ChannelHandlerContext ctx) throws DialogsDeviation, InvalidParameterSpecException, InvalidKeyException {
        byte[] nextMsg = cryptoState.getNextSetupMessage();
        ctx.writeAndFlush(nextMsg);
    }

    private void channelReadEntity(ChannelHandlerContext ctx, byte[] msg) throws DialogsDeviation, InvalidAlgorithmParameterException, InvalidKeyException {
        byte[] data = cryptoState.decrypt(msg);

        handler.handle(ctx.channel().attr(DialogsConnection.CONNECTION_ATTR).get(), data);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (cryptoState.isReady()) {
            // we can send user data now, we just have to encrypt it first...
            byte[] data = cryptoState.encrypt((byte[]) msg);
            super.write(ctx, data, promise);
        } else {
            throw new DialogsDeviation("Trying to send data, but cryptostate isn't ready yet!");
        }
    }

    /**
     * Wait for authentication to be completed
     * @param timeoutmillis the amount of time to wait...
     * @throws DialogsDeviation
     */
    public void awaitForAuth(long timeoutmillis) throws DialogsDeviation {
        try {
            authenticatedPromise.await(timeoutmillis);
            if (!authenticatedPromise.isSuccess()) {
                awaitForAuthCoordinator();
            }
        } catch (InterruptedException e) {
            throw new DialogsDeviation(e);
        }
    }

    private void awaitForAuthCoordinator() throws DialogsDeviation {
        new DialogsNettyHandlerHelp().invoke();
    }

    private class DialogsNettyHandlerEntity {
        private ChannelHandlerContext ctx;

        public DialogsNettyHandlerEntity(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        public void invoke() {
            ctx.close();
            authenticatedPromise.setFailure(new DialogsDeviation("Failed handshake"));
        }
    }

    private class DialogsNettyHandlerHelp {
        public void invoke() throws DialogsDeviation {
            throw new DialogsDeviation(authenticatedPromise.cause().getMessage());
        }
    }
}
