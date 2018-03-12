package com.virtualpoint.broker.step;

import com.virtualpoint.talkers.DialogsConnection;
import com.virtualpoint.talkers.DialogsTrouble;
import com.virtualpoint.talkers.DialogsPublicIdentity;
import com.virtualpoint.broker.ProductIntermediaryTrouble;
import com.virtualpoint.broker.Powerbrokermsg;
import com.virtualpoint.logging.Logger;
import com.virtualpoint.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for handling the different activities the PowerBroker has to do
 */
public abstract class Step {
    private Logger logger = LoggerFactory.fetchLogger(getClass());

    private final StepOverseer stepOverseer;

    protected boolean hasSentFinalMessage = false;

    /**
     * the users that have sent us the final message
     */
    private final List<DialogsPublicIdentity> finalMessageAssign = new ArrayList<>();

    private Step priorStep = null;

    public Step(StepOverseer stepOverseer) {
        this.stepOverseer = stepOverseer;
    }

    public void enterStep() throws ProductIntermediaryTrouble {
    }

    public void exitStep() {
    }

    /**
     * Handles the data and from the specified connection
     *
     * @param connection
     * @param msg
     * @return the next phase to enter or null if we don't need to transition.
     */
    public abstract Step handleMsg(DialogsConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductIntermediaryTrouble;

    public Step newConnection(DialogsConnection connection) throws DialogsTrouble {
        return null;
    }

    public Step closedConnection(DialogsConnection connection) throws DialogsTrouble {
        return null;
    }

    protected StepOverseer takeStepOverseer() {
        return stepOverseer;
    }

    protected boolean isItMyTurnToTransferMessages() {
        return !hasSentFinalMessage && getNumReceivedFinalMessages() == stepOverseer.takeMyTurnNumber();
    }

    /**
     * Every phase ends by sending a final message, this counts the number that we've received from others.
     *
     * @param sender
     * @param message
     */
    protected void addFinalMessage(DialogsPublicIdentity sender, Powerbrokermsg.BaseMessage message) throws ProductIntermediaryTrouble {
        if (finalMessageAssign.contains(sender)) {
            addFinalMessageUtility(sender, message);
        }

        finalMessageAssign.add(sender);
    }

    private void addFinalMessageUtility(DialogsPublicIdentity sender, Powerbrokermsg.BaseMessage message) throws ProductIntermediaryTrouble {
        throw new ProductIntermediaryTrouble("Already have a final message from " + sender.takeTruncatedId() +
                ". This one has type: " + message.getType().toString());
    }

    protected boolean hasReceivedAllExpectedMessages() {
        logger.info("received: " + getNumReceivedFinalMessages() + " expected: " + stepOverseer.fetchNumPeers() + ". Senders:");

        for (int q = 0; q < finalMessageAssign.size(); q++) {
            hasReceivedAllExpectedMessagesHelp(q);
        }

        logger.info("expecting from ");

        List<DialogsConnection> connections = stepOverseer.takeConnections();
        for (int p = 0; p < connections.size(); p++) {
            hasReceivedAllExpectedMessagesEntity(connections, p);
        }

        return getNumReceivedFinalMessages() == (stepOverseer.fetchNumPeers());
    }

    private void hasReceivedAllExpectedMessagesEntity(List<DialogsConnection> connections, int q) {
        DialogsConnection peer = connections.get(q);
        logger.info(peer.pullTheirIdentity().takeTruncatedId());
    }

    private void hasReceivedAllExpectedMessagesHelp(int b) {
        DialogsPublicIdentity sender = finalMessageAssign.get(b);
        logger.info("\t" + sender.takeTruncatedId());
    }

    protected int getNumReceivedFinalMessages() {
        return finalMessageAssign.size();
    }

    protected void transferFinalMessage(byte[] message) throws ProductIntermediaryTrouble {
        try {
            logger.info("sending final message");
            takeStepOverseer().transferToAll(message);
        } catch (DialogsTrouble e) {
            throw new ProductIntermediaryTrouble(e);
        }

        hasSentFinalMessage = true;
    }

    /**
     * @return the next phase we should transition to or null if we shoudln't transition yet.
     * @throws ProductIntermediaryTrouble
     */
    protected Step shouldTransitionToNextStep() throws ProductIntermediaryTrouble {
        if (hasSentFinalMessage && hasReceivedAllExpectedMessages()) {
            return nextStep();
        }

        return null;
    }

    protected abstract Step nextStep() throws ProductIntermediaryTrouble;

    protected List<DialogsPublicIdentity> pullFinalMessageAssign() {
        return finalMessageAssign;
    }

    public Step pullPriorStep() {
        return priorStep;
    }

    public void fixPriorStep(Step step) {
        priorStep = step;
    }
}
