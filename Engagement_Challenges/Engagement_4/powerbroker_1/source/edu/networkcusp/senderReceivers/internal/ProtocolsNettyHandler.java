package edu.networkcusp.senderReceivers.internal;

import edu.networkcusp.senderReceivers.ProtocolsConnection;
import edu.networkcusp.senderReceivers.ProtocolsRaiser;
import edu.networkcusp.senderReceivers.ProtocolsHandler;
import edu.networkcusp.senderReceivers.ProtocolsIdentity;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.Promise;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;


/**
 * Used in the Netty framework to do our auth and crypto
 */
public class ProtocolsNettyHandler extends ChannelDuplexHandler { //extends SimpleChannelInboundHandler<byte[]> {
    private final ProtocolsHandler handler;
    private final ProtocolsCryptoState cryptoState;
    private final boolean isServer;
    private Promise authenticatedPromise;

    /**
     *
     * @param handler the CommsHandler that will be notified when data arrives or connections close or open
     * @param identity the identity of the entity using this handler (i.e. this end of the connection)
     * @param isServer true if this is being used as a server
     * @param authenticatedPromise signaled when we're successfully authenticated
     * @throws ProtocolsRaiser
     */
    public ProtocolsNettyHandler(ProtocolsHandler handler, ProtocolsIdentity identity, boolean isServer, Promise authenticatedPromise) throws ProtocolsRaiser {
        this.handler = handler;
        this.cryptoState = new ProtocolsCryptoState(identity);
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
        ProtocolsConnection connection = ctx.channel().attr(ProtocolsConnection.CONNECTION_ATTR).get();
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
            channelReadCoordinator(ctx, (byte[]) msg);
        } else {
            // if it isn't ready, the this should be a setup message, process it as one
            cryptoState.processNextSetupMessage((byte[]) msg);

            // does it have another setup message to send?
            if (cryptoState.hasSetupMessage()) {
                byte[] nextMsg = cryptoState.grabNextSetupMessage();
                ctx.writeAndFlush(nextMsg);
            }


            // has the RSA authentication test failed?
            if (cryptoState.hasFailed()) {
                // if it has, close the connection
                new ProtocolsNettyHandlerCoordinator(ctx).invoke();
            }

            // is it ready now (are we all authenticated and everything)?
            if (cryptoState.isReady()) {
                channelReadExecutor(ctx);
            }
        }
    }

    private void channelReadExecutor(ChannelHandlerContext ctx) throws ProtocolsRaiser {
        Channel ch = ctx.channel();
        ProtocolsConnection connection = new ProtocolsConnection(ch, cryptoState.getTheirIdentity());
        ch.attr(ProtocolsConnection.CONNECTION_ATTR).set(connection);

        // clients will be waiting for this event
        authenticatedPromise.setSuccess(null);

        // the server will want to know about the new connection
        // we don't even notify it until authentication is complete
        if (isServer) {
            new ProtocolsNettyHandlerService(connection).invoke();
        }
    }

    private void channelReadCoordinator(ChannelHandlerContext ctx, byte[] msg) throws ProtocolsRaiser, InvalidAlgorithmParameterException, InvalidKeyException {
        byte[] data = cryptoState.decrypt(msg);

        handler.handle(ctx.channel().attr(ProtocolsConnection.CONNECTION_ATTR).get(), data);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (cryptoState.isReady()) {
            // we can send user data now, we just have to encrypt it first...
            byte[] data = cryptoState.encrypt((byte[]) msg);
            super.write(ctx, data, promise);
        } else {
            writeAid();
        }
    }

    private void writeAid() throws ProtocolsRaiser {
        throw new ProtocolsRaiser("Trying to send data, but cryptostate isn't ready yet!");
    }

    /**
     * Wait for authentication to be completed
     * @param timeoutmillis the amount of time to wait...
     * @throws ProtocolsRaiser
     */
    public void awaitForAuthorize(long timeoutmillis) throws ProtocolsRaiser {
        try {
            authenticatedPromise.await(timeoutmillis);
            if (!authenticatedPromise.isSuccess()) {
                awaitForAuthorizeHerder();
            }
        } catch (InterruptedException e) {
            throw new ProtocolsRaiser(e);
        }
    }

    private void awaitForAuthorizeHerder() throws ProtocolsRaiser {
        throw new ProtocolsRaiser(authenticatedPromise.cause().getMessage());
    }

    private class ProtocolsNettyHandlerCoordinator {
        private ChannelHandlerContext ctx;

        public ProtocolsNettyHandlerCoordinator(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        public void invoke() {
            ctx.close();
            authenticatedPromise.setFailure(new ProtocolsRaiser("Failed handshake"));
        }
    }

    private class ProtocolsNettyHandlerService {
        private ProtocolsConnection connection;

        public ProtocolsNettyHandlerService(ProtocolsConnection connection) {
            this.connection = connection;
        }

        public void invoke() throws ProtocolsRaiser {
            handler.newConnection(connection);
        }
    }
}
