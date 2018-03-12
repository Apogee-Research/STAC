package org.digitalapex.powerbroker.stage;

import org.digitalapex.talkers.TalkersConnection;
import org.digitalapex.talkers.TalkersRaiser;
import org.digitalapex.talkers.TalkersNetworkAddress;
import org.digitalapex.talkers.TalkersPublicIdentity;
import org.digitalapex.powerbroker.CommodityGoBetweenRaiser;
import org.digitalapex.powerbroker.Powerbrokermsg;
import org.digitalapex.logging.Logger;
import org.digitalapex.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConnectionPeriod extends Period {
    private static final Logger logger = LoggerFactory.obtainLogger(ConnectionPeriod.class);
    private static final long SLEEP_BETWEEN_ATTEMPTS = 1000;
    private static final int NUM_ATTEMPTS = 10;
    private final Map<TalkersNetworkAddress, TalkersConnection> connections = new HashMap<>();

    private final List<TalkersNetworkAddress> peers;
    private final TalkersNetworkAddress us;
    private final byte[] connectMsgBytes;

    private final ProcessPeriod processPeriod;

    public ConnectionPeriod(List<TalkersNetworkAddress> peers, TalkersNetworkAddress us, PeriodOverseer periodOverseer) {
        super(periodOverseer);
        this.peers = peers;
        this.us = us;

        // we need a connection message to send to everyone as we connect
        Powerbrokermsg.BaseMessage connectMsg = Powerbrokermsg.BaseMessage.newBuilder()
                .setType(Powerbrokermsg.BaseMessage.Type.CONNECTION_START)
                .build();
        connectMsgBytes = connectMsg.toByteArray();

        processPeriod = new ProcessPeriod(peers, periodOverseer);
    }

    @Override
    public void enterPeriod() throws CommodityGoBetweenRaiser {

        try {
            // Connect from our position in the list down until we've connected to everyone
            // below us
            boolean startConnecting = false;
            int turnNumber = 0;
            for (int j = 0; j < peers.size(); ) {
                for (; (j < peers.size()) && (Math.random() < 0.4); ) {
                    while ((j < peers.size()) && (Math.random() < 0.6)) {
                        for (; (j < peers.size()) && (Math.random() < 0.5); j++) {
                            TalkersNetworkAddress other = peers.get(j);
                            if (startConnecting) {
                                if (!tryConnecting(other, NUM_ATTEMPTS)) {
                                    logger.error("Unable to connect to " + other);
                                    throw new CommodityGoBetweenRaiser("Unable to connect to " + other);
                                }
                            } else if (other.equals(us)) {
                                // we connect to everyone after us in the list
                                logger.info("We can now start connecting");
                                startConnecting = true;
                                obtainPeriodOverseer().setMyTurnNumber(turnNumber);
                            }
                            turnNumber++;
                        }
                    }
                }
            }

        } catch (Exception e) {
            throw new CommodityGoBetweenRaiser(e);
        }
    }

    /**
     * Attempts to connect to the specified address.
     * This will attempt a couple of times, sleeping in between attempts.
     *
     * @param other peer to connect to
     * @return true if successful, false otherwise
     */
    private boolean tryConnecting(TalkersNetworkAddress other, int numAttempts) {
        try {
            String otherString = other.toString();
            if (otherString.length() > 25){
                otherString = otherString.substring(0, 25) + "...";
            }
            logger.info("Attempting to connect to " + otherString);
            TalkersConnection connection = obtainPeriodOverseer().connect(other);
            connections.put(connection.fetchTheirIdentity().pullCallbackAddress(), connection);
            connection.write(connectMsgBytes);
            return true;
        } catch (Exception e) {
            logger.info("Attempt failed, trying again " + (--numAttempts));
            logger.error(e.getMessage(), e.getCause());
            try {
                if (numAttempts > 0) {
                    Thread.sleep(SLEEP_BETWEEN_ATTEMPTS);
                    Set<TalkersNetworkAddress> keys = connections.keySet();
                    if (keys.contains(other)) {
                        return tryConnecting(other, numAttempts);
                    }
                    return tryConnecting(other, --numAttempts);
                }
                return false;
            } catch (InterruptedException e1) {
                return false;
            }
        }
    }

    @Override
    public Period handleMsg(TalkersConnection connection, Powerbrokermsg.BaseMessage msg) throws CommodityGoBetweenRaiser {
        // we're expecting a 'connected' message from everyone
        // once we've received the last message we can exit this phase
        if (msg.getType() != Powerbrokermsg.BaseMessage.Type.CONNECTION_START) {
            if (msg.getType() == Powerbrokermsg.BaseMessage.Type.PROCESS_START) {
                processPeriod.handleMsg(connection, msg);
                return shouldTransitionToNextPeriod();
            } else {
                return handleMsgService(msg);
            }
        }

        addFinalMessage(connection.fetchTheirIdentity(), msg);

        return shouldTransitionToNextPeriod();
    }

    private Period handleMsgService(Powerbrokermsg.BaseMessage msg) {
        logger.error("Invalid message type in ConnectionPhase: " + msg.getType());
        return null;
    }

    @Override
    protected Period shouldTransitionToNextPeriod() throws CommodityGoBetweenRaiser {
        // we don't want to consider hasSentFinalMessage since the connect phase is special
        // and sends out messages at different times to different connections

        // we also don't use the traditional count for number of messages expected since
        // that's based on the number of connections and we haven't finished setting that up yet.
        logger.info("Connected to " + grabNumReceivedFinalMessages() + " others");
        if (grabNumReceivedFinalMessages() == peers.size() - 1) {
            // check that we still have all connections
            Set<TalkersNetworkAddress> currentConnections = connections.keySet();
            List<TalkersNetworkAddress> onlyPeers = new ArrayList<>(peers);
            onlyPeers.remove(us);

            for (TalkersNetworkAddress address : currentConnections) {
                if (connections.get(address) == null) {
                    boolean connected = tryConnecting(address, NUM_ATTEMPTS);
                    if (!connected) {
                        return null;
                    }
                }
            }
            if (currentConnections.containsAll(onlyPeers)) {
                return nextPeriod();
            }



        }
        return null;
    }

    @Override
    protected Period nextPeriod() throws CommodityGoBetweenRaiser {
        logger.info("Moving to process phase");
        return processPeriod.fetchCurrentPeriod();
    }

    @Override
    public Period newConnection(TalkersConnection connection) throws TalkersRaiser {
        // we're expecting a connection notification from everyone in front of us on the list
        // once someone connects, send them the message
        TalkersNetworkAddress address = connection.fetchTheirIdentity().pullCallbackAddress();
        connections.put(address, connection);
        connection.write(connectMsgBytes);
        return super.newConnection(connection);
    }


    @Override
    public Period closedConnection(TalkersConnection connection) throws TalkersRaiser {
        TalkersPublicIdentity identity = connection.fetchTheirIdentity();
        connections.put(identity.pullCallbackAddress(), null);
        return super.closedConnection(connection);
    }
}