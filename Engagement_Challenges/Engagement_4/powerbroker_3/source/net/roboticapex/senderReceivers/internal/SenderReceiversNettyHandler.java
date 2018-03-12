package net.roboticapex.senderReceivers.internal;

import net.roboticapex.senderReceivers.SenderReceiversConnection;
import net.roboticapex.senderReceivers.SenderReceiversDeviation;
import net.roboticapex.senderReceivers.SenderReceiversHandler;
import net.roboticapex.senderReceivers.SenderReceiversIdentity;
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
public class SenderReceiversNettyHandler extends ChannelDuplexHandler { //extends SimpleChannelInboundHandler<byte[]> {
    private final SenderReceiversHandler handler;
    private final SenderReceiversCryptoState cryptoState;
    private final boolean isServer;
    private Promise authenticatedPromise;

    /**
     *
     * @param handler the CommsHandler that will be notified when data arrives or connections close or open
     * @param identity the identity of the entity using this handler (i.e. this end of the connection)
     * @param isServer true if this is being used as a server
     * @param authenticatedPromise signaled when we're successfully authenticated
     * @throws SenderReceiversDeviation
     */
    public SenderReceiversNettyHandler(SenderReceiversHandler handler, SenderReceiversIdentity identity, boolean isServer, Promise authenticatedPromise) throws SenderReceiversDeviation {
        this.handler = handler;
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
            byte[] setupMsg = cryptoState.getNextSetupMessage();
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
            handler.closedConnection(connection);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (cryptoState.isReady()) {
            // If the cryptoState is ready, then this is just regular user data, give it to them
            // after we decrypt it
            channelReadHandler(ctx, (byte[]) msg);
        } else {
            // if it isn't ready, the this should be a setup message, process it as one
            cryptoState.processNextSetupMessage((byte[]) msg);

            // does it have another setup message to send?
            if (cryptoState.hasSetupMessage()) {
                channelReadAid(ctx);
            }


            // has the RSA authentication test failed?
            if (cryptoState.hasFailed()) {
                // if it has, close the connection
                ctx.close();
                authenticatedPromise.setFailure(new SenderReceiversDeviation("Failed handshake"));
            }

            // is it ready now (are we all authenticated and everything)?
            if (cryptoState.isReady()) {
                Channel ch = ctx.channel();
                SenderReceiversConnection connection = new SenderReceiversConnection(ch, cryptoState.getTheirIdentity());
                ch.attr(SenderReceiversConnection.CONNECTION_ATTR).set(connection);

                // clients will be waiting for this event
                authenticatedPromise.setSuccess(null);

                // the server will want to know about the new connection
                // we don't even notify it until authentication is complete
                if (isServer) {
                    handler.newConnection(connection);
                }
            }
        }
    }

    private void channelReadAid(ChannelHandlerContext ctx) throws SenderReceiversDeviation, InvalidParameterSpecException, InvalidKeyException {
        byte[] nextMsg = cryptoState.getNextSetupMessage();
        ctx.writeAndFlush(nextMsg);
    }

    private void channelReadHandler(ChannelHandlerContext ctx, byte[] msg) throws SenderReceiversDeviation, InvalidAlgorithmParameterException, InvalidKeyException {
        byte[] data = cryptoState.decrypt(msg);

        handler.handle(ctx.channel().attr(SenderReceiversConnection.CONNECTION_ATTR).get(), data);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (cryptoState.isReady()) {
            // we can send user data now, we just have to encrypt it first...
            writeSupervisor(ctx, (byte[]) msg, promise);
        } else {
            throw new SenderReceiversDeviation("Trying to send data, but cryptostate isn't ready yet!");
        }
    }

    private void writeSupervisor(ChannelHandlerContext ctx, byte[] msg, ChannelPromise promise) throws Exception {
        byte[] data = cryptoState.encrypt(msg);
        super.write(ctx, data, promise);
    }

    /**
     * Wait for authentication to be completed
     * @param timeoutmillis the amount of time to wait...
     * @throws SenderReceiversDeviation
     */
    public void awaitForAuthorize(long timeoutmillis) throws SenderReceiversDeviation {
        try {
            authenticatedPromise.await(timeoutmillis);
            if (!authenticatedPromise.isSuccess()) {
                awaitForAuthorizeEngine();
            }
        } catch (InterruptedException e) {
            throw new SenderReceiversDeviation(e);
        }
    }

    private void awaitForAuthorizeEngine() throws SenderReceiversDeviation {
        throw new SenderReceiversDeviation(authenticatedPromise.cause().getMessage());
    }
}
