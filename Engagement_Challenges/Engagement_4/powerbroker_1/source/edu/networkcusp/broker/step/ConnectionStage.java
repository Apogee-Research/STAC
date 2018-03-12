package edu.networkcusp.broker.step;

import edu.networkcusp.senderReceivers.ProtocolsConnection;
import edu.networkcusp.senderReceivers.ProtocolsRaiser;
import edu.networkcusp.senderReceivers.ProtocolsNetworkAddress;
import edu.networkcusp.senderReceivers.ProtocolsPublicIdentity;
import edu.networkcusp.broker.ProductIntermediaryRaiser;
import edu.networkcusp.broker.Powerbrokermsg;
import edu.networkcusp.logging.Logger;
import edu.networkcusp.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConnectionStage extends Stage {
    private static final Logger logger = LoggerFactory.pullLogger(ConnectionStage.class);
    private static final long SLEEP_BETWEEN_ATTEMPTS = 1000;
    private static final int NUM_ATTEMPTS = 10;
    private final Map<ProtocolsNetworkAddress, ProtocolsConnection> connections = new HashMap<>();

    private final List<ProtocolsNetworkAddress> peers;
    private final ProtocolsNetworkAddress us;
    private final byte[] connectMsgBytes;

    private final ProcessStage processStage;

    public ConnectionStage(List<ProtocolsNetworkAddress> peers, ProtocolsNetworkAddress us, StageOverseer stageOverseer) {
        super(stageOverseer);
        this.peers = peers;
        this.us = us;

        // we need a connection message to send to everyone as we connect
        Powerbrokermsg.BaseMessage connectMsg = Powerbrokermsg.BaseMessage.newBuilder()
                .setType(Powerbrokermsg.BaseMessage.Type.CONNECTION_START)
                .build();
        connectMsgBytes = connectMsg.toByteArray();

        processStage = new ProcessStage(peers, stageOverseer);
    }

    @Override
    public void enterStage() throws ProductIntermediaryRaiser {

        try {
            // Connect from our position in the list down until we've connected to everyone
            // below us
            boolean startConnecting = false;
            int turnNumber = 0;
            for (int i = 0; i < peers.size(); i++) {
                ProtocolsNetworkAddress other = peers.get(i);
                if (startConnecting) {
                    if (!tryConnecting(other, NUM_ATTEMPTS)) {
                        logger.error("Unable to connect to " + other);
                        throw new ProductIntermediaryRaiser("Unable to connect to " + other);
                    }
                } else if (other.equals(us)) {
                    // we connect to everyone after us in the list
                    logger.info("We can now start connecting");
                    startConnecting = true;
                    takeStageOverseer().assignMyTurnNumber(turnNumber);
                }
                turnNumber++;
            }

        } catch (Exception e) {
            throw new ProductIntermediaryRaiser(e);
        }
    }

    /**
     * Attempts to connect to the specified address.
     * This will attempt a couple of times, sleeping in between attempts.
     *
     * @param other peer to connect to
     * @return true if successful, false otherwise
     */
    private boolean tryConnecting(ProtocolsNetworkAddress other, int numAttempts) {
        try {
            String otherString = other.toString();
            if (otherString.length() > 25){
                otherString = otherString.substring(0, 25) + "...";
            }
            logger.info("Attempting to connect to " + otherString);
            ProtocolsConnection connection = takeStageOverseer().connect(other);
            connections.put(connection.takeTheirIdentity().takeCallbackAddress(), connection);
            connection.write(connectMsgBytes);
            return true;
        } catch (Exception e) {
            logger.info("Attempt failed, trying again " + (numAttempts));
            logger.error(e.getMessage(), e.getCause());
            try {
                if (numAttempts > 0) {
                    Thread.sleep(SLEEP_BETWEEN_ATTEMPTS);
                    Set<ProtocolsNetworkAddress> keys = connections.keySet();
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
    public Stage handleMsg(ProtocolsConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductIntermediaryRaiser {
        // we're expecting a 'connected' message from everyone
        // once we've received the last message we can exit this phase
        if (msg.getType() != Powerbrokermsg.BaseMessage.Type.CONNECTION_START) {
            if (msg.getType() == Powerbrokermsg.BaseMessage.Type.PROCESS_START) {
                processStage.handleMsg(connection, msg);
                return shouldTransitionToNextStage();
            } else {
                logger.error("Invalid message type in ConnectionPhase: " + msg.getType());
                return null;
            }
        }

        addFinalMessage(connection.takeTheirIdentity(), msg);

        return shouldTransitionToNextStage();
    }

    @Override
    protected Stage shouldTransitionToNextStage() throws ProductIntermediaryRaiser {
        // we don't want to consider hasSentFinalMessage since the connect phase is special
        // and sends out messages at different times to different connections

        // we also don't use the traditional count for number of messages expected since
        // that's based on the number of connections and we haven't finished setting that up yet.
        logger.info("Connected to " + pullNumReceivedFinalMessages() + " others");
        if (pullNumReceivedFinalMessages() == peers.size() - 1) {
            // check that we still have all connections
            Set<ProtocolsNetworkAddress> currentConnections = connections.keySet();
            List<ProtocolsNetworkAddress> onlyPeers = new ArrayList<>(peers);
            onlyPeers.remove(us);

            for (ProtocolsNetworkAddress address : currentConnections) {
                if (connections.get(address) == null) {
                    boolean connected = tryConnecting(address, NUM_ATTEMPTS);
                    if (!connected) {
                        return null;
                    }
                }
            }
            if (currentConnections.containsAll(onlyPeers)) {
                return nextStage();
            }



        }
        return null;
    }

    @Override
    protected Stage nextStage() throws ProductIntermediaryRaiser {
        logger.info("Moving to process phase");
        return processStage.fetchCurrentStage();
    }

    @Override
    public Stage newConnection(ProtocolsConnection connection) throws ProtocolsRaiser {
        // we're expecting a connection notification from everyone in front of us on the list
        // once someone connects, send them the message
        ProtocolsNetworkAddress address = connection.takeTheirIdentity().takeCallbackAddress();
        connections.put(address, connection);
        connection.write(connectMsgBytes);
        return super.newConnection(connection);
    }


    @Override
    public Stage closedConnection(ProtocolsConnection connection) throws ProtocolsRaiser {
        ProtocolsPublicIdentity identity = connection.takeTheirIdentity();
        connections.put(identity.takeCallbackAddress(), null);
        return super.closedConnection(connection);
    }
}