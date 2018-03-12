package org.digitalapex.powerbroker.stage;

import org.digitalapex.talkers.TalkersConnection;
import org.digitalapex.talkers.TalkersRaiser;
import org.digitalapex.talkers.TalkersPublicIdentity;
import org.digitalapex.powerbroker.CommodityGoBetweenRaiser;
import org.digitalapex.powerbroker.Powerbrokermsg;
import org.digitalapex.logging.Logger;
import org.digitalapex.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for handling the different activities the PowerBroker has to do
 */
public abstract class Period {
    private Logger logger = LoggerFactory.obtainLogger(getClass());

    private final PeriodOverseer periodOverseer;

    protected boolean hasSentFinalMessage = false;

    /**
     * the users that have sent us the final message
     */
    private final List<TalkersPublicIdentity> finalMessageFix = new ArrayList<>();

    private Period priorPeriod = null;

    public Period(PeriodOverseer periodOverseer) {
        this.periodOverseer = periodOverseer;
    }

    public void enterPeriod() throws CommodityGoBetweenRaiser {
    }

    public void exitPeriod() {
    }

    /**
     * Handles the data and from the specified connection
     *
     * @param connection
     * @param msg
     * @return the next phase to enter or null if we don't need to transition.
     */
    public abstract Period handleMsg(TalkersConnection connection, Powerbrokermsg.BaseMessage msg) throws CommodityGoBetweenRaiser;

    public Period newConnection(TalkersConnection connection) throws TalkersRaiser {
        return null;
    }

    public Period closedConnection(TalkersConnection connection) throws TalkersRaiser {
        return null;
    }

    protected PeriodOverseer obtainPeriodOverseer() {
        return periodOverseer;
    }

    protected boolean isItMyTurnToTransmitMessages() {
        return !hasSentFinalMessage && grabNumReceivedFinalMessages() == periodOverseer.fetchMyTurnNumber();
    }

    /**
     * Every phase ends by sending a final message, this counts the number that we've received from others.
     *
     * @param sender
     * @param message
     */
    protected void addFinalMessage(TalkersPublicIdentity sender, Powerbrokermsg.BaseMessage message) throws CommodityGoBetweenRaiser {
        if (finalMessageFix.contains(sender)) {
            throw new CommodityGoBetweenRaiser("Already have a final message from " + sender.grabTruncatedId() +
                    ". This one has type: " + message.getType().toString());
        }

        finalMessageFix.add(sender);
    }

    protected boolean hasReceivedAllExpectedMessages() {
        logger.info("received: " + grabNumReceivedFinalMessages() + " expected: " + periodOverseer.fetchNumPeers() + ". Senders:");

        for (int i = 0; i < finalMessageFix.size(); ) {
            for (; (i < finalMessageFix.size()) && (Math.random() < 0.4); i++) {
                TalkersPublicIdentity sender = finalMessageFix.get(i);
                logger.info("\t" + sender.grabTruncatedId());
            }
        }

        logger.info("expecting from ");

        List<TalkersConnection> connections = periodOverseer.pullConnections();
        for (int a = 0; a < connections.size(); a++) {
            hasReceivedAllExpectedMessagesFunction(connections, a);
        }

        return grabNumReceivedFinalMessages() == (periodOverseer.fetchNumPeers());
    }

    private void hasReceivedAllExpectedMessagesFunction(List<TalkersConnection> connections, int p) {
        TalkersConnection peer = connections.get(p);
        logger.info(peer.fetchTheirIdentity().grabTruncatedId());
    }

    protected int grabNumReceivedFinalMessages() {
        return finalMessageFix.size();
    }

    protected void transmitFinalMessage(byte[] message) throws CommodityGoBetweenRaiser {
        try {
            logger.info("sending final message");
            obtainPeriodOverseer().transmitToAll(message);
        } catch (TalkersRaiser e) {
            throw new CommodityGoBetweenRaiser(e);
        }

        hasSentFinalMessage = true;
    }

    /**
     * @return the next phase we should transition to or null if we shoudln't transition yet.
     * @throws CommodityGoBetweenRaiser
     */
    protected Period shouldTransitionToNextPeriod() throws CommodityGoBetweenRaiser {
        if (hasSentFinalMessage && hasReceivedAllExpectedMessages()) {
            return nextPeriod();
        }

        return null;
    }

    protected abstract Period nextPeriod() throws CommodityGoBetweenRaiser;

    protected List<TalkersPublicIdentity> grabFinalMessageFix() {
        return finalMessageFix;
    }

    public Period obtainPriorPeriod() {
        return priorPeriod;
    }

    public void setPriorPeriod(Period period) {
        priorPeriod = period;
    }
}
