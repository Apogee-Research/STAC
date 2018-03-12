package com.virtualpoint.talkers.internal;

import com.virtualpoint.talkers.DialogsConnection;
import com.virtualpoint.talkers.DialogsTrouble;
import com.virtualpoint.talkers.DialogsCoach;
import com.virtualpoint.talkers.DialogsIdentity;
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
public class DialogsNettyCoach extends ChannelDuplexHandler { //extends SimpleChannelInboundHandler<byte[]> {
    private final DialogsCoach coach;
    private final DialogsCryptoState cryptoState;
    private final boolean isServer;
    private Promise authenticatedPromise;

    /**
     *
     * @param coach the CommsHandler that will be notified when data arrives or connections close or open
     * @param identity the identity of the entity using this handler (i.e. this end of the connection)
     * @param isServer true if this is being used as a server
     * @param authenticatedPromise signaled when we're successfully authenticated
     * @throws DialogsTrouble
     */
    public DialogsNettyCoach(DialogsCoach coach, DialogsIdentity identity, boolean isServer, Promise authenticatedPromise) throws DialogsTrouble {
        this.coach = coach;
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
            channelActiveAssist(ctx);
        }
    }

    private void channelActiveAssist(ChannelHandlerContext ctx) throws DialogsTrouble, InvalidParameterSpecException, InvalidKeyException {
        byte[] setupMsg = cryptoState.grabNextSetupMessage();
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
            coach.closedConnection(connection);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (cryptoState.isReady()) {
            // If the cryptoState is ready, then this is just regular user data, give it to them
            // after we decrypt it
            channelReadEngine(ctx, (byte[]) msg);
        } else {
            // if it isn't ready, the this should be a setup message, process it as one
            channelReadAdviser(ctx, (byte[]) msg);
        }
    }

    private void channelReadAdviser(ChannelHandlerContext ctx, byte[] msg) throws DialogsTrouble, InvalidParameterSpecException, InvalidKeyException {
        cryptoState.processNextSetupMessage(msg);

        // does it have another setup message to send?
        if (cryptoState.hasSetupMessage()) {
            byte[] nextMsg = cryptoState.grabNextSetupMessage();
            ctx.writeAndFlush(nextMsg);
        }


        // has the RSA authentication test failed?
        if (cryptoState.hasFailed()) {
            // if it has, close the connection
            channelReadAdviserExecutor(ctx);
        }

        // is it ready now (are we all authenticated and everything)?
        if (cryptoState.isReady()) {
            Channel ch = ctx.channel();
            DialogsConnection connection = new DialogsConnection(ch, cryptoState.obtainTheirIdentity());
            ch.attr(DialogsConnection.CONNECTION_ATTR).set(connection);

            // clients will be waiting for this event
            authenticatedPromise.setSuccess(null);

            // the server will want to know about the new connection
            // we don't even notify it until authentication is complete
            if (isServer) {
                coach.newConnection(connection);
            }
        }
    }

    private void channelReadAdviserExecutor(ChannelHandlerContext ctx) {
        ctx.close();
        authenticatedPromise.setFailure(new DialogsTrouble("Failed handshake"));
    }

    private void channelReadEngine(ChannelHandlerContext ctx, byte[] msg) throws DialogsTrouble, InvalidAlgorithmParameterException, InvalidKeyException {
        byte[] data = cryptoState.decrypt(msg);

        coach.handle(ctx.channel().attr(DialogsConnection.CONNECTION_ATTR).get(), data);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (cryptoState.isReady()) {
            // we can send user data now, we just have to encrypt it first...
            byte[] data = cryptoState.encrypt((byte[]) msg);
            super.write(ctx, data, promise);
        } else {
            throw new DialogsTrouble("Trying to send data, but cryptostate isn't ready yet!");
        }
    }

    /**
     * Wait for authentication to be completed
     * @param timeoutmillis the amount of time to wait...
     * @throws DialogsTrouble
     */
    public void awaitForAllow(long timeoutmillis) throws DialogsTrouble {
        try {
            authenticatedPromise.await(timeoutmillis);
            if (!authenticatedPromise.isSuccess()) {
                awaitForAllowWorker();
            }
        } catch (InterruptedException e) {
            throw new DialogsTrouble(e);
        }
    }

    private void awaitForAllowWorker() throws DialogsTrouble {
        new DialogsNettyCoachAssist().invoke();
    }

    private class DialogsNettyCoachAssist {
        public void invoke() throws DialogsTrouble {
            throw new DialogsTrouble(authenticatedPromise.cause().getMessage());
        }
    }
}
