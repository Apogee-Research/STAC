package net.roboticapex.broker.period;

import net.roboticapex.senderReceivers.SenderReceiversConnection;
import net.roboticapex.senderReceivers.SenderReceiversDeviation;
import net.roboticapex.senderReceivers.SenderReceiversNetworkAddress;
import net.roboticapex.senderReceivers.SenderReceiversPublicIdentity;
import net.roboticapex.broker.ProductLiaisonDeviation;
import net.roboticapex.broker.Powerbrokermsg;
import net.roboticapex.logging.Logger;
import net.roboticapex.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConnectionStep extends Step {
    private static final Logger logger = LoggerFactory.fetchLogger(ConnectionStep.class);
    private static final long SLEEP_BETWEEN_ATTEMPTS = 1000;
    private static final int NUM_ATTEMPTS = 10;
    private final Map<SenderReceiversNetworkAddress, SenderReceiversConnection> connections = new HashMap<>();

    private final List<SenderReceiversNetworkAddress> peers;
    private final SenderReceiversNetworkAddress us;
    private final byte[] connectMsgBytes;

    private final ProcessStep processStep;

    public ConnectionStep(List<SenderReceiversNetworkAddress> peers, SenderReceiversNetworkAddress us, StepOverseer stepOverseer) {
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
    public void enterStep() throws ProductLiaisonDeviation {

        try {
            // Connect from our position in the list down until we've connected to everyone
            // below us
            boolean startConnecting = false;
            int turnNumber = 0;
            for (int p = 0; p < peers.size(); p++) {
                SenderReceiversNetworkAddress other = peers.get(p);
                if (startConnecting) {
                    enterStepHerder(other);
                } else if (other.equals(us)) {
                    // we connect to everyone after us in the list
                    logger.info("We can now start connecting");
                    startConnecting = true;
                    grabStepOverseer().assignMyTurnNumber(turnNumber);
                }
                turnNumber++;
            }

        } catch (Exception e) {
            throw new ProductLiaisonDeviation(e);
        }
    }

    private void enterStepHerder(SenderReceiversNetworkAddress other) throws ProductLiaisonDeviation {
        if (!tryConnecting(other, NUM_ATTEMPTS)) {
            logger.error("Unable to connect to " + other);
            throw new ProductLiaisonDeviation("Unable to connect to " + other);
        }
    }

    /**
     * Attempts to connect to the specified address.
     * This will attempt a couple of times, sleeping in between attempts.
     *
     * @param other peer to connect to
     * @return true if successful, false otherwise
     */
    private boolean tryConnecting(SenderReceiversNetworkAddress other, int numAttempts) {
        try {
            String otherString = other.toString();
            if (otherString.length() > 25){
                otherString = otherString.substring(0, 25) + "...";
            }
            logger.info("Attempting to connect to " + otherString);
            SenderReceiversConnection connection = grabStepOverseer().connect(other);
            connections.put(connection.obtainTheirIdentity().pullCallbackAddress(), connection);
            connection.write(connectMsgBytes);
            return true;
        } catch (Exception e) {
            logger.info("Attempt failed, trying again " + (--numAttempts));
            logger.error(e.getMessage(), e.getCause());
            try {
                if (numAttempts > 0) {
                    Thread.sleep(SLEEP_BETWEEN_ATTEMPTS);
                    Set<SenderReceiversNetworkAddress> keys = connections.keySet();
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
    public Step handleMsg(SenderReceiversConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductLiaisonDeviation {
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

        addFinalMessage(connection.obtainTheirIdentity(), msg);

        return shouldTransitionToNextStep();
    }

    @Override
    protected Step shouldTransitionToNextStep() throws ProductLiaisonDeviation {
        // we don't want to consider hasSentFinalMessage since the connect phase is special
        // and sends out messages at different times to different connections

        // we also don't use the traditional count for number of messages expected since
        // that's based on the number of connections and we haven't finished setting that up yet.
        logger.info("Connected to " + pullNumReceivedFinalMessages() + " others");
        if (pullNumReceivedFinalMessages() == peers.size() - 1) {
            // check that we still have all connections
            Set<SenderReceiversNetworkAddress> currentConnections = connections.keySet();
            List<SenderReceiversNetworkAddress> onlyPeers = new ArrayList<>(peers);
            onlyPeers.remove(us);

            for (SenderReceiversNetworkAddress address : currentConnections) {
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
    protected Step nextStep() throws ProductLiaisonDeviation {
        logger.info("Moving to process phase");
        return processStep.takeCurrentStep();
    }

    @Override
    public Step newConnection(SenderReceiversConnection connection) throws SenderReceiversDeviation {
        // we're expecting a connection notification from everyone in front of us on the list
        // once someone connects, send them the message
        SenderReceiversNetworkAddress address = connection.obtainTheirIdentity().pullCallbackAddress();
        connections.put(address, connection);
        connection.write(connectMsgBytes);
        return super.newConnection(connection);
    }


    @Override
    public Step closedConnection(SenderReceiversConnection connection) throws SenderReceiversDeviation {
        SenderReceiversPublicIdentity identity = connection.obtainTheirIdentity();
        connections.put(identity.pullCallbackAddress(), null);
        return super.closedConnection(connection);
    }
}