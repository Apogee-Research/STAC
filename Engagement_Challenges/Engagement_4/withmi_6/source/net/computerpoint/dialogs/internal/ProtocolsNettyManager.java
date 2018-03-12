package net.computerpoint.dialogs.internal;

import net.computerpoint.dialogs.ProtocolsConnection;
import net.computerpoint.dialogs.ProtocolsDeviation;
import net.computerpoint.dialogs.ProtocolsManager;
import net.computerpoint.dialogs.ProtocolsIdentity;
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
public class ProtocolsNettyManager extends ChannelDuplexHandler { //extends SimpleChannelInboundHandler<byte[]> {
    private final ProtocolsManager manager;
    private final ProtocolsCryptoState cryptoState;
    private final boolean isServer;
    private Promise authenticatedPromise;

    /**
     *
     * @param conductor the CommsHandler that will be notified when data arrives or connections close or open
     * @param identity the identity of the entity using this handler (i.e. this end of the connection)
     * @param isServer true if this is being used as a server
     * @param authenticatedPromise signaled when we're successfully authenticated
     * @throws ProtocolsDeviation
     */
    public ProtocolsNettyManager(ProtocolsManager conductor, ProtocolsIdentity identity, boolean isServer, Promise authenticatedPromise) throws ProtocolsDeviation {
        this.manager = conductor;
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
            channelActiveGateKeeper(ctx);
        }
    }

    private void channelActiveGateKeeper(ChannelHandlerContext ctx) throws ProtocolsDeviation, InvalidParameterSpecException, InvalidKeyException {
        new ProtocolsNettyManagerHelper(ctx).invoke();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        // TCP connection was just closed
        ProtocolsConnection connection = ctx.channel().attr(ProtocolsConnection.CONNECTION_ATTR).get();
        // the connection may be null, if we did not successfully connect to the other user
        // if that's the case, we don't need to disconnect from the other user
        if (connection != null) {
            channelInactiveFunction(connection);
        }
    }

    private void channelInactiveFunction(ProtocolsConnection connection) throws ProtocolsDeviation {
        manager.closedConnection(connection);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (cryptoState.isReady()) {
            // If the cryptoState is ready, then this is just regular user data, give it to them
            // after we decrypt it
            channelReadEngine(ctx, (byte[]) msg);
        } else {
            // if it isn't ready, the this should be a setup message, process it as one
            cryptoState.processNextSetupMessage((byte[]) msg);

            // does it have another setup message to send?
            if (cryptoState.hasSetupMessage()) {
                byte[] nextMsg = cryptoState.pullNextSetupMessage();
                ctx.writeAndFlush(nextMsg);
            }


            // has the RSA authentication test failed?
            if (cryptoState.hasFailed()) {
                // if it has, close the connection
                new ProtocolsNettyManagerEngine(ctx).invoke();
            }

            // is it ready now (are we all authenticated and everything)?
            if (cryptoState.isReady()) {
                Channel ch = ctx.channel();
                ProtocolsConnection connection = new ProtocolsConnection(ch, cryptoState.fetchTheirIdentity());
                ch.attr(ProtocolsConnection.CONNECTION_ATTR).set(connection);

                // clients will be waiting for this event
                authenticatedPromise.setSuccess(null);

                // the server will want to know about the new connection
                // we don't even notify it until authentication is complete
                if (isServer) {
                    channelReadHerder(connection);
                }
            }
        }
    }

    private void channelReadHerder(ProtocolsConnection connection) throws ProtocolsDeviation {
        manager.newConnection(connection);
    }

    private void channelReadEngine(ChannelHandlerContext ctx, byte[] msg) throws ProtocolsDeviation, InvalidAlgorithmParameterException, InvalidKeyException {
        new ProtocolsNettyManagerHelp(ctx, msg).invoke();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (cryptoState.isReady()) {
            // we can send user data now, we just have to encrypt it first...
            byte[] data = cryptoState.encrypt((byte[]) msg);
            super.write(ctx, data, promise);
        } else {
            writeHelper();
        }
    }

    private void writeHelper() throws ProtocolsDeviation {
        throw new ProtocolsDeviation("Trying to send data, but cryptostate isn't ready yet!");
    }

    /**
     * Wait for authentication to be completed
     * @param timeoutmillis the amount of time to wait...
     * @throws ProtocolsDeviation
     */
    public void awaitForAuthorize(long timeoutmillis) throws ProtocolsDeviation {
        try {
            authenticatedPromise.await(timeoutmillis);
            if (!authenticatedPromise.isSuccess()) {
                awaitForAuthorizeSupervisor();
            }
        } catch (InterruptedException e) {
            throw new ProtocolsDeviation(e);
        }
    }

    private void awaitForAuthorizeSupervisor() throws ProtocolsDeviation {
        throw new ProtocolsDeviation(authenticatedPromise.cause().getMessage());
    }

    private class ProtocolsNettyManagerHelper {
        private ChannelHandlerContext ctx;

        public ProtocolsNettyManagerHelper(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        public void invoke() throws ProtocolsDeviation, InvalidParameterSpecException, InvalidKeyException {
            byte[] setupMsg = cryptoState.pullNextSetupMessage();
            ctx.writeAndFlush(setupMsg);
        }
    }

    private class ProtocolsNettyManagerHelp {
        private ChannelHandlerContext ctx;
        private byte[] msg;

        public ProtocolsNettyManagerHelp(ChannelHandlerContext ctx, byte[] msg) {
            this.ctx = ctx;
            this.msg = msg;
        }

        public void invoke() throws ProtocolsDeviation, InvalidAlgorithmParameterException, InvalidKeyException {
            byte[] data = cryptoState.decrypt(msg);

            manager.handle(ctx.channel().attr(ProtocolsConnection.CONNECTION_ATTR).get(), data);
        }
    }

    private class ProtocolsNettyManagerEngine {
        private ChannelHandlerContext ctx;

        public ProtocolsNettyManagerEngine(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        public void invoke() {
            ctx.close();
            authenticatedPromise.setFailure(new ProtocolsDeviation("Failed handshake"));
        }
    }
}
