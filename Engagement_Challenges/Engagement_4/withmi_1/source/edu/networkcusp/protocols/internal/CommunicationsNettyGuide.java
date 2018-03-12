package edu.networkcusp.protocols.internal;

import edu.networkcusp.protocols.CommunicationsConnection;
import edu.networkcusp.protocols.CommunicationsFailure;
import edu.networkcusp.protocols.CommunicationsGuide;
import edu.networkcusp.protocols.CommunicationsIdentity;
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
public class CommunicationsNettyGuide extends ChannelDuplexHandler { //extends SimpleChannelInboundHandler<byte[]> {
    private final CommunicationsGuide guide;
    private final CommunicationsCryptoState cryptoState;
    private final boolean isServer;
    private Promise authenticatedPromise;

    /**
     *
     * @param guide the CommsHandler that will be notified when data arrives or connections close or open
     * @param identity the identity of the entity using this handler (i.e. this end of the connection)
     * @param isServer true if this is being used as a server
     * @param authenticatedPromise signaled when we're successfully authenticated
     * @throws CommunicationsFailure
     */
    public CommunicationsNettyGuide(CommunicationsGuide guide, CommunicationsIdentity identity, boolean isServer, Promise authenticatedPromise) throws CommunicationsFailure {
        this.guide = guide;
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
            channelActiveHome(ctx);
        }
    }

    private void channelActiveHome(ChannelHandlerContext ctx) throws CommunicationsFailure, InvalidParameterSpecException, InvalidKeyException {
        byte[] setupMsg = cryptoState.obtainNextSetupMessage();
        ctx.writeAndFlush(setupMsg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        // TCP connection was just closed
        CommunicationsConnection connection = ctx.channel().attr(CommunicationsConnection.CONNECTION_ATTR).get();
        // the connection may be null, if we did not successfully connect to the other user
        // if that's the case, we don't need to disconnect from the other user
        if (connection != null) {
            channelInactiveEntity(connection);
        }
    }

    private void channelInactiveEntity(CommunicationsConnection connection) throws CommunicationsFailure {
        guide.closedConnection(connection);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (cryptoState.isReady()) {
            // If the cryptoState is ready, then this is just regular user data, give it to them
            // after we decrypt it
            byte[] data = cryptoState.decrypt((byte[]) msg);

            guide.handle(ctx.channel().attr(CommunicationsConnection.CONNECTION_ATTR).get(), data);
        } else {
            // if it isn't ready, the this should be a setup message, process it as one
            channelReadUtility(ctx, (byte[]) msg);
        }
    }

    private void channelReadUtility(ChannelHandlerContext ctx, byte[] msg) throws CommunicationsFailure, InvalidParameterSpecException, InvalidKeyException {
        cryptoState.processNextSetupMessage(msg);

        // does it have another setup message to send?
        if (cryptoState.hasSetupMessage()) {
            byte[] nextMsg = cryptoState.obtainNextSetupMessage();
            ctx.writeAndFlush(nextMsg);
        }


        // has the RSA authentication test failed?
        if (cryptoState.hasFailed()) {
            // if it has, close the connection
            ctx.close();
            authenticatedPromise.setFailure(new CommunicationsFailure("Failed handshake"));
        }

        // is it ready now (are we all authenticated and everything)?
        if (cryptoState.isReady()) {
            new CommunicationsNettyGuideHelp(ctx).invoke();
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (cryptoState.isReady()) {
            // we can send user data now, we just have to encrypt it first...
            byte[] data = cryptoState.encrypt((byte[]) msg);
            super.write(ctx, data, promise);
        } else {
            throw new CommunicationsFailure("Trying to send data, but cryptostate isn't ready yet!");
        }
    }

    /**
     * Wait for authentication to be completed
     * @param timeoutmillis the amount of time to wait...
     * @throws CommunicationsFailure
     */
    public void awaitForPermission(long timeoutmillis) throws CommunicationsFailure {
        try {
            authenticatedPromise.await(timeoutmillis);
            if (!authenticatedPromise.isSuccess()) {
                new CommunicationsNettyGuideGuide().invoke();
            }
        } catch (InterruptedException e) {
            throw new CommunicationsFailure(e);
        }
    }

    private class CommunicationsNettyGuideHelp {
        private ChannelHandlerContext ctx;

        public CommunicationsNettyGuideHelp(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        public void invoke() throws CommunicationsFailure {
            Channel ch = ctx.channel();
            CommunicationsConnection connection = new CommunicationsConnection(ch, cryptoState.grabTheirIdentity());
            ch.attr(CommunicationsConnection.CONNECTION_ATTR).set(connection);

            // clients will be waiting for this event
            authenticatedPromise.setSuccess(null);

            // the server will want to know about the new connection
            // we don't even notify it until authentication is complete
            if (isServer) {
                invokeAid(connection);
            }
        }

        private void invokeAid(CommunicationsConnection connection) throws CommunicationsFailure {
            guide.newConnection(connection);
        }
    }

    private class CommunicationsNettyGuideGuide {
        public void invoke() throws CommunicationsFailure {
            throw new CommunicationsFailure(authenticatedPromise.cause().getMessage());
        }
    }
}
