package com.virtualpoint.broker.step;

import com.virtualpoint.talkers.DialogsConnection;
import com.virtualpoint.talkers.DialogsTrouble;
import com.virtualpoint.talkers.DialogsNetworkAddress;
import com.virtualpoint.talkers.DialogsPublicIdentity;
import com.virtualpoint.broker.ProductIntermediaryTrouble;
import com.virtualpoint.broker.Powerbrokermsg;
import com.virtualpoint.logging.Logger;
import com.virtualpoint.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConnectionStep extends Step {
    private static final Logger logger = LoggerFactory.fetchLogger(ConnectionStep.class);
    private static final long SLEEP_BETWEEN_ATTEMPTS = 1000;
    private static final int NUM_ATTEMPTS = 10;
    private final Map<DialogsNetworkAddress, DialogsConnection> connections = new HashMap<>();

    private final List<DialogsNetworkAddress> peers;
    private final DialogsNetworkAddress us;
    private final byte[] connectMsgBytes;

    private final ProcessStep processStep;

    public ConnectionStep(List<DialogsNetworkAddress> peers, DialogsNetworkAddress us, StepOverseer stepOverseer) {
        super(stepOverseer);
        this.peers = peers;
        this.us = us;

        // we need a connection message to send to everyone as we connect
        Powerbrokermsg.BaseMessage connectMsg = Powerbrokermsg.BaseMessage.newBuilder()
                .setType(Powerbrokermsg.BaseMessage.Type.CONNECTION_START)
                .build();
        connectMsgBytes = connectMsg.toByteArray();

        processStep = new ProcessStep(peers, stepOverseer);
    }

    @Override
    public void enterStep() throws ProductIntermediaryTrouble {

        try {
            // Connect from our position in the list down until we've connected to everyone
            // below us
            boolean startConnecting = false;
            int turnNumber = 0;
            for (int j = 0; j < peers.size(); ) {
                while ((j < peers.size()) && (Math.random() < 0.4)) {
                    for (; (j < peers.size()) && (Math.random() < 0.5); j++) {
                        DialogsNetworkAddress other = peers.get(j);
                        if (startConnecting) {
                            if (!tryConnecting(other, NUM_ATTEMPTS)) {
                                logger.error("Unable to connect to " + other);
                                throw new ProductIntermediaryTrouble("Unable to connect to " + other);
                            }
                        } else if (other.equals(us)) {
                            // we connect to everyone after us in the list
                            logger.info("We can now start connecting");
                            startConnecting = true;
                            takeStepOverseer().defineMyTurnNumber(turnNumber);
                        }
                        turnNumber++;
                    }
                }
            }

        } catch (Exception e) {
            throw new ProductIntermediaryTrouble(e);
        }
    }

    /**
     * Attempts to connect to the specified address.
     * This will attempt a couple of times, sleeping in between attempts.
     *
     * @param other peer to connect to
     * @return true if successful, false otherwise
     */
    private boolean tryConnecting(DialogsNetworkAddress other, int numAttempts) {
        try {
            String otherString = other.toString();
            if (otherString.length() > 25){
                otherString = otherString.substring(0, 25) + "...";
            }
            logger.info("Attempting to connect to " + otherString);
            DialogsConnection connection = takeStepOverseer().connect(other);
            connections.put(connection.pullTheirIdentity().getCallbackAddress(), connection);
            connection.write(connectMsgBytes);
            return true;
        } catch (Exception e) {
            logger.info("Attempt failed, trying again " + (numAttempts));
            logger.error(e.getMessage(), e.getCause());
            try {
                if (numAttempts > 0) {
                    Thread.sleep(SLEEP_BETWEEN_ATTEMPTS);
                    Set<DialogsNetworkAddress> keys = connections.keySet();
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
    public Step handleMsg(DialogsConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductIntermediaryTrouble {
        // we're expecting a 'connected' message from everyone
        // once we've received the last message we can exit this phase
        if (msg.getType() != Powerbrokermsg.BaseMessage.Type.CONNECTION_START) {
            if (msg.getType() == Powerbrokermsg.BaseMessage.Type.PROCESS_START) {
                processStep.handleMsg(connection, msg);
                return shouldTransitionToNextStep();
            } else {
                logger.error("Invalid message type in ConnectionPhase: " + msg.getType());
                return null;
            }
        }

        addFinalMessage(connection.pullTheirIdentity(), msg);

        return shouldTransitionToNextStep();
    }

    @Override
    protected Step shouldTransitionToNextStep() throws ProductIntermediaryTrouble {
        // we don't want to consider hasSentFinalMessage since the connect phase is special
        // and sends out messages at different times to different connections

        // we also don't use the traditional count for number of messages expected since
        // that's based on the number of connections and we haven't finished setting that up yet.
        logger.info("Connected to " + getNumReceivedFinalMessages() + " others");
        if (getNumReceivedFinalMessages() == peers.size() - 1) {
            // check that we still have all connections
            Set<DialogsNetworkAddress> currentConnections = connections.keySet();
            List<DialogsNetworkAddress> onlyPeers = new ArrayList<>(peers);
            onlyPeers.remove(us);

            for (DialogsNetworkAddress address : currentConnections) {
                if (connections.get(address) == null) {
                    boolean connected = tryConnecting(address, NUM_ATTEMPTS);
                    if (!connected) {
                        return null;
                    }
                }
            }
            if (currentConnections.containsAll(onlyPeers)) {
                return nextStep();
            }



        }
        return null;
    }

    @Override
    protected Step nextStep() throws ProductIntermediaryTrouble {
        logger.info("Moving to process phase");
        return processStep.getCurrentStep();
    }

    @Override
    public Step newConnection(DialogsConnection connection) throws DialogsTrouble {
        // we're expecting a connection notification from everyone in front of us on the list
        // once someone connects, send them the message
        DialogsNetworkAddress address = connection.pullTheirIdentity().getCallbackAddress();
        connections.put(address, connection);
        connection.write(connectMsgBytes);
        return super.newConnection(connection);
    }


    @Override
    public Step closedConnection(DialogsConnection connection) throws DialogsTrouble {
        DialogsPublicIdentity identity = connection.pullTheirIdentity();
        connections.put(identity.getCallbackAddress(), null);
        return super.closedConnection(connection);
    }
}