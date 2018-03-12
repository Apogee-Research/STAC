package edu.computerapex.dialogs.internal;

import edu.computerapex.dialogs.CommunicationsConnection;
import edu.computerapex.dialogs.CommunicationsDeviation;
import edu.computerapex.dialogs.CommunicationsHandler;
import edu.computerapex.dialogs.CommunicationsIdentity;
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
public class CommunicationsNettyHandler extends ChannelDuplexHandler { //extends SimpleChannelInboundHandler<byte[]> {
    private final CommunicationsHandler handler;
    private final CommunicationsCryptoState cryptoState;
    private final boolean isServer;
    private Promise authenticatedPromise;

    /**
     *
     * @param handler the CommsHandler that will be notified when data arrives or connections close or open
     * @param identity the identity of the entity using this handler (i.e. this end of the connection)
     * @param isServer true if this is being used as a server
     * @param authenticatedPromise signaled when we're successfully authenticated
     * @throws CommunicationsDeviation
     */
    public CommunicationsNettyHandler(CommunicationsHandler handler, CommunicationsIdentity identity, boolean isServer, Promise authenticatedPromise) throws CommunicationsDeviation {
        this.handler = handler;
        this.cryptoState = new CommunicationsCryptoState(identity);
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
            new CommunicationsNettyHandlerEngine(ctx).invoke();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        // TCP connection was just closed
        CommunicationsConnection connection = ctx.channel().attr(CommunicationsConnection.CONNECTION_ATTR).get();
        // the connection may be null, if we did not successfully connect to the other user
        // if that's the case, we don't need to disconnect from the other user
        if (connection != null) {
            channelInactiveCoordinator(connection);
        }
    }

    private void channelInactiveCoordinator(CommunicationsConnection connection) throws CommunicationsDeviation {
        handler.closedConnection(connection);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (cryptoState.isReady()) {
            // If the cryptoState is ready, then this is just regular user data, give it to them
            // after we decrypt it
            byte[] data = cryptoState.decrypt((byte[]) msg);

            handler.handle(ctx.channel().attr(CommunicationsConnection.CONNECTION_ATTR).get(), data);
        } else {
            // if it isn't ready, the this should be a setup message, process it as one
            channelReadHelp(ctx, (byte[]) msg);
        }
    }

    private void channelReadHelp(ChannelHandlerContext ctx, byte[] msg) throws CommunicationsDeviation, InvalidParameterSpecException, InvalidKeyException {
        cryptoState.processNextSetupMessage(msg);

        // does it have another setup message to send?
        if (cryptoState.hasSetupMessage()) {
            channelReadHelpService(ctx);
        }


        // has the RSA authentication test failed?
        if (cryptoState.hasFailed()) {
            // if it has, close the connection
            channelReadHelpWorker(ctx);
        }

        // is it ready now (are we all authenticated and everything)?
        if (cryptoState.isReady()) {
            channelReadHelpAssist(ctx);
        }
    }

    private void channelReadHelpAssist(ChannelHandlerContext ctx) throws CommunicationsDeviation {
        Channel ch = ctx.channel();
        CommunicationsConnection connection = new CommunicationsConnection(ch, cryptoState.obtainTheirIdentity());
        ch.attr(CommunicationsConnection.CONNECTION_ATTR).set(connection);

        // clients will be waiting for this event
        authenticatedPromise.setSuccess(null);

        // the server will want to know about the new connection
        // we don't even notify it until authentication is complete
        if (isServer) {
            handler.newConnection(connection);
        }
    }

    private void channelReadHelpWorker(ChannelHandlerContext ctx) {
        ctx.close();
        authenticatedPromise.setFailure(new CommunicationsDeviation("Failed handshake"));
    }

    private void channelReadHelpService(ChannelHandlerContext ctx) throws CommunicationsDeviation, InvalidParameterSpecException, InvalidKeyException {
        byte[] nextMsg = cryptoState.obtainNextSetupMessage();
        ctx.writeAndFlush(nextMsg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (cryptoState.isReady()) {
            // we can send user data now, we just have to encrypt it first...
            writeHerder(ctx, (byte[]) msg, promise);
        } else {
            writeHome();
        }
    }

    private void writeHome() throws CommunicationsDeviation {
        throw new CommunicationsDeviation("Trying to send data, but cryptostate isn't ready yet!");
    }

    private void writeHerder(ChannelHandlerContext ctx, byte[] msg, ChannelPromise promise) throws Exception {
        byte[] data = cryptoState.encrypt(msg);
        super.write(ctx, data, promise);
    }

    /**
     * Wait for authentication to be completed
     * @param timeoutmillis the amount of time to wait...
     * @throws CommunicationsDeviation
     */
    public void awaitForPermission(long timeoutmillis) throws CommunicationsDeviation {
        try {
            authenticatedPromise.await(timeoutmillis);
            if (!authenticatedPromise.isSuccess()) {
                awaitForPermissionExecutor();
            }
        } catch (InterruptedException e) {
            throw new CommunicationsDeviation(e);
        }
    }

    private void awaitForPermissionExecutor() throws CommunicationsDeviation {
        throw new CommunicationsDeviation(authenticatedPromise.cause().getMessage());
    }

    private class CommunicationsNettyHandlerEngine {
        private ChannelHandlerContext ctx;

        public CommunicationsNettyHandlerEngine(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        public void invoke() throws CommunicationsDeviation, InvalidParameterSpecException, InvalidKeyException {
            byte[] setupMsg = cryptoState.obtainNextSetupMessage();
            ctx.writeAndFlush(setupMsg);
        }
    }
}
