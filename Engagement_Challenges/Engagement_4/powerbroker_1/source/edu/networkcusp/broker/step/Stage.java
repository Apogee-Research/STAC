package edu.networkcusp.broker.step;

import edu.networkcusp.senderReceivers.ProtocolsConnection;
import edu.networkcusp.senderReceivers.ProtocolsRaiser;
import edu.networkcusp.senderReceivers.ProtocolsPublicIdentity;
import edu.networkcusp.broker.ProductIntermediaryRaiser;
import edu.networkcusp.broker.Powerbrokermsg;
import edu.networkcusp.logging.Logger;
import edu.networkcusp.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for handling the different activities the PowerBroker has to do
 */
public abstract class Stage {
    private Logger logger = LoggerFactory.pullLogger(getClass());

    private final StageOverseer stageOverseer;

    protected boolean hasSentFinalMessage = false;

    /**
     * the users that have sent us the final message
     */
    private final List<ProtocolsPublicIdentity> finalMessageSet = new ArrayList<>();

    private Stage priorStage = null;

    public Stage(StageOverseer stageOverseer) {
        this.stageOverseer = stageOverseer;
    }

    public void enterStage() throws ProductIntermediaryRaiser {
    }

    public void exitStage() {
    }

    /**
     * Handles the data and from the specified connection
     *
     * @param connection
     * @param msg
     * @return the next phase to enter or null if we don't need to transition.
     */
    public abstract Stage handleMsg(ProtocolsConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductIntermediaryRaiser;

    public Stage newConnection(ProtocolsConnection connection) throws ProtocolsRaiser {
        return null;
    }

    public Stage closedConnection(ProtocolsConnection connection) throws ProtocolsRaiser {
        return null;
    }

    protected StageOverseer takeStageOverseer() {
        return stageOverseer;
    }

    protected boolean isItMyTurnToSendMessages() {
        return !hasSentFinalMessage && pullNumReceivedFinalMessages() == stageOverseer.getMyTurnNumber();
    }

    /**
     * Every phase ends by sending a final message, this counts the number that we've received from others.
     *
     * @param sender
     * @param message
     */
    protected void addFinalMessage(ProtocolsPublicIdentity sender, Powerbrokermsg.BaseMessage message) throws ProductIntermediaryRaiser {
        if (finalMessageSet.contains(sender)) {
            throw new ProductIntermediaryRaiser("Already have a final message from " + sender.obtainTruncatedId() +
                    ". This one has type: " + message.getType().toString());
        }

        finalMessageSet.add(sender);
    }

    protected boolean hasReceivedAllExpectedMessages() {
        logger.info("received: " + pullNumReceivedFinalMessages() + " expected: " + stageOverseer.fetchNumPeers() + ". Senders:");

        for (int i = 0; i < finalMessageSet.size(); i++) {
            ProtocolsPublicIdentity sender = finalMessageSet.get(i);
            logger.info("\t" + sender.obtainTruncatedId());
        }

        logger.info("expecting from ");

        List<ProtocolsConnection> connections = stageOverseer.grabConnections();
        for (int p = 0; p < connections.size(); p++) {
            ProtocolsConnection peer = connections.get(p);
            logger.info(peer.takeTheirIdentity().obtainTruncatedId());
        }

        return pullNumReceivedFinalMessages() == (stageOverseer.fetchNumPeers());
    }

    protected int pullNumReceivedFinalMessages() {
        return finalMessageSet.size();
    }

    protected void sendFinalMessage(byte[] message) throws ProductIntermediaryRaiser {
        try {
            logger.info("sending final message");
            takeStageOverseer().sendToAll(message);
        } catch (ProtocolsRaiser e) {
            throw new ProductIntermediaryRaiser(e);
        }

        hasSentFinalMessage = true;
    }

    /**
     * @return the next phase we should transition to or null if we shoudln't transition yet.
     * @throws ProductIntermediaryRaiser
     */
    protected Stage shouldTransitionToNextStage() throws ProductIntermediaryRaiser {
        if (hasSentFinalMessage && hasReceivedAllExpectedMessages()) {
            return nextStage();
        }

        return null;
    }

    protected abstract Stage nextStage() throws ProductIntermediaryRaiser;

    protected List<ProtocolsPublicIdentity> pullFinalMessageAssign() {
        return finalMessageSet;
    }

    public Stage getPriorStage() {
        return priorStage;
    }

    public void fixPriorStage(Stage stage) {
        priorStage = stage;
    }
}
