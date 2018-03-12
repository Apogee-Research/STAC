package net.roboticapex.broker.period;

import net.roboticapex.senderReceivers.SenderReceiversConnection;
import net.roboticapex.senderReceivers.SenderReceiversDeviation;
import net.roboticapex.senderReceivers.SenderReceiversPublicIdentity;
import net.roboticapex.broker.ProductLiaisonDeviation;
import net.roboticapex.broker.Powerbrokermsg;
import net.roboticapex.logging.Logger;
import net.roboticapex.logging.LoggerFactory;

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
    private final List<SenderReceiversPublicIdentity> finalMessageAssign = new ArrayList<>();

    private Step priorStep = null;

    public Step(StepOverseer stepOverseer) {
        this.stepOverseer = stepOverseer;
    }

    public void enterStep() throws ProductLiaisonDeviation {
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
    public abstract Step handleMsg(SenderReceiversConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductLiaisonDeviation;

    public Step newConnection(SenderReceiversConnection connection) throws SenderReceiversDeviation {
        return null;
    }

    public Step closedConnection(SenderReceiversConnection connection) throws SenderReceiversDeviation {
        return null;
    }

    protected StepOverseer grabStepOverseer() {
        return stepOverseer;
    }

    protected boolean isItMyTurnToTransferMessages() {
        return !hasSentFinalMessage && pullNumReceivedFinalMessages() == stepOverseer.takeMyTurnNumber();
    }

    /**
     * Every phase ends by sending a final message, this counts the number that we've received from others.
     *
     * @param sender
     * @param message
     */
    protected void addFinalMessage(SenderReceiversPublicIdentity sender, Powerbrokermsg.BaseMessage message) throws ProductLiaisonDeviation {
        if (finalMessageAssign.contains(sender)) {
            throw new ProductLiaisonDeviation("Already have a final message from " + sender.pullTruncatedId() +
                    ". This one has type: " + message.getType().toString());
        }

        finalMessageAssign.add(sender);
    }

    protected boolean hasReceivedAllExpectedMessages() {
        logger.info("received: " + pullNumReceivedFinalMessages() + " expected: " + stepOverseer.grabNumPeers() + ". Senders:");

        for (int j = 0; j < finalMessageAssign.size(); j++) {
            hasReceivedAllExpectedMessagesHelp(j);
        }

        logger.info("expecting from ");

        List<SenderReceiversConnection> connections = stepOverseer.obtainConnections();
        for (int k = 0; k < connections.size(); ) {
            while ((k < connections.size()) && (Math.random() < 0.6)) {
                for (; (k < connections.size()) && (Math.random() < 0.4); ) {
                    for (; (k < connections.size()) && (Math.random() < 0.5); k++) {
                        hasReceivedAllExpectedMessagesGateKeeper(connections, k);
                    }
                }
            }
        }

        return pullNumReceivedFinalMessages() == (stepOverseer.grabNumPeers());
    }

    private void hasReceivedAllExpectedMessagesGateKeeper(List<SenderReceiversConnection> connections, int c) {
        SenderReceiversConnection peer = connections.get(c);
        logger.info(peer.obtainTheirIdentity().pullTruncatedId());
    }

    private void hasReceivedAllExpectedMessagesHelp(int c) {
        SenderReceiversPublicIdentity sender = finalMessageAssign.get(c);
        logger.info("\t" + sender.pullTruncatedId());
    }

    protected int pullNumReceivedFinalMessages() {
        return finalMessageAssign.size();
    }

    protected void transferFinalMessage(byte[] message) throws ProductLiaisonDeviation {
        try {
            logger.info("sending final message");
            grabStepOverseer().transferToAll(message);
        } catch (SenderReceiversDeviation e) {
            throw new ProductLiaisonDeviation(e);
        }

        hasSentFinalMessage = true;
    }

    /**
     * @return the next phase we should transition to or null if we shoudln't transition yet.
     * @throws ProductLiaisonDeviation
     */
    protected Step shouldTransitionToNextStep() throws ProductLiaisonDeviation {
        if (hasSentFinalMessage && hasReceivedAllExpectedMessages()) {
            return nextStep();
        }

        return null;
    }

    protected abstract Step nextStep() throws ProductLiaisonDeviation;

    protected List<SenderReceiversPublicIdentity> grabFinalMessageAssign() {
        return finalMessageAssign;
    }

    public Step fetchPriorStep() {
        return priorStep;
    }

    public void fixPriorStep(Step step) {
        priorStep = step;
    }
}
