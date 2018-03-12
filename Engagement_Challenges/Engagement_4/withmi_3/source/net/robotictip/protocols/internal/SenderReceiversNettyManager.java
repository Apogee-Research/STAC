package net.robotictip.protocols.internal;

import net.robotictip.protocols.SenderReceiversConnection;
import net.robotictip.protocols.SenderReceiversConnectionBuilder;
import net.robotictip.protocols.SenderReceiversTrouble;
import net.robotictip.protocols.SenderReceiversManager;
import net.robotictip.protocols.SenderReceiversIdentity;
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
public class SenderReceiversNettyManager extends ChannelDuplexHandler { //extends SimpleChannelInboundHandler<byte[]> {
    private final SenderReceiversManager manager;
    private final SenderReceiversCryptoState cryptoState;
    private final boolean isServer;
    private Promise authenticatedPromise;

    /**
     *
     * @param manager the CommsHandler that will be notified when data arrives or connections close or open
     * @param identity the identity of the entity using this handler (i.e. this end of the connection)
     * @param isServer true if this is being used as a server
     * @param authenticatedPromise signaled when we're successfully authenticated
     * @throws SenderReceiversTrouble
     */
    public SenderReceiversNettyManager(SenderReceiversManager manager, SenderReceiversIdentity identity, boolean isServer, Promise authenticatedPromise) throws SenderReceiversTrouble {
        this.manager = manager;
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
            channelInactiveEngine(connection);
        }
    }

    private void channelInactiveEngine(SenderReceiversConnection connection) throws SenderReceiversTrouble {
        manager.closedConnection(connection);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (cryptoState.isReady()) {
            // If the cryptoState is ready, then this is just regular user data, give it to them
            // after we decrypt it
            new SenderReceiversNettyManagerAdviser(ctx, (byte[]) msg).invoke();
        } else {
            // if it isn't ready, the this should be a setup message, process it as one
            cryptoState.processNextSetupMessage((byte[]) msg);

            // does it have another setup message to send?
            if (cryptoState.hasSetupMessage()) {
                new SenderReceiversNettyManagerEngine(ctx).invoke();
            }


            // has the RSA authentication test failed?
            if (cryptoState.hasFailed()) {
                // if it has, close the connection
                ctx.close();
                authenticatedPromise.setFailure(new SenderReceiversTrouble("Failed handshake"));
            }

            // is it ready now (are we all authenticated and everything)?
            if (cryptoState.isReady()) {
                Channel ch = ctx.channel();
                SenderReceiversConnection connection = new SenderReceiversConnectionBuilder().setChannel(ch).fixTheirIdentity(cryptoState.fetchTheirIdentity()).generateSenderReceiversConnection();
                ch.attr(SenderReceiversConnection.CONNECTION_ATTR).set(connection);

                // clients will be waiting for this event
                authenticatedPromise.setSuccess(null);

                // the server will want to know about the new connection
                // we don't even notify it until authentication is complete
                if (isServer) {
                    manager.newConnection(connection);
                }
            }
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (cryptoState.isReady()) {
            // we can send user data now, we just have to encrypt it first...
            writeWorker(ctx, (byte[]) msg, promise);
        } else {
            throw new SenderReceiversTrouble("Trying to send data, but cryptostate isn't ready yet!");
        }
    }

    private void writeWorker(ChannelHandlerContext ctx, byte[] msg, ChannelPromise promise) throws Exception {
        byte[] data = cryptoState.encrypt(msg);
        super.write(ctx, data, promise);
    }

    /**
     * Wait for authentication to be completed
     * @param timeoutmillis the amount of time to wait...
     * @throws SenderReceiversTrouble
     */
    public void awaitForAuthorize(long timeoutmillis) throws SenderReceiversTrouble {
        try {
            authenticatedPromise.await(timeoutmillis);
            if (!authenticatedPromise.isSuccess()) {
                throw new SenderReceiversTrouble(authenticatedPromise.cause().getMessage());
            }
        } catch (InterruptedException e) {
            throw new SenderReceiversTrouble(e);
        }
    }

    private class SenderReceiversNettyManagerAdviser {
        private ChannelHandlerContext ctx;
        private byte[] msg;

        public SenderReceiversNettyManagerAdviser(ChannelHandlerContext ctx, byte[] msg) {
            this.ctx = ctx;
            this.msg = msg;
        }

        public void invoke() throws SenderReceiversTrouble, InvalidAlgorithmParameterException, InvalidKeyException {
            byte[] data = cryptoState.decrypt(msg);

            manager.handle(ctx.channel().attr(SenderReceiversConnection.CONNECTION_ATTR).get(), data);
        }
    }

    private class SenderReceiversNettyManagerEngine {
        private ChannelHandlerContext ctx;

        public SenderReceiversNettyManagerEngine(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        public void invoke() throws SenderReceiversTrouble, InvalidParameterSpecException, InvalidKeyException {
            byte[] nextMsg = cryptoState.pullNextSetupMessage();
            ctx.writeAndFlush(nextMsg);
        }
    }
}
