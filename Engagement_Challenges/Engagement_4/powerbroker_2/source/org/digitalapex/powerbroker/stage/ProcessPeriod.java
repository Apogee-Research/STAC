package org.digitalapex.powerbroker.stage;

import org.digitalapex.talkers.TalkersConnection;
import org.digitalapex.talkers.TalkersNetworkAddress;
import org.digitalapex.powerbroker.CommodityGoBetweenRaiser;
import org.digitalapex.powerbroker.Powerbrokermsg;
import org.digitalapex.logging.Logger;
import org.digitalapex.logging.LoggerFactory;

import java.util.List;

/**
 * Phase Class that acts like a latch that waits until all connections
 * have completed (in ConnectionPhase) before moving to the next phase.
 * This class and ConnectionPhase are coupled because connection start
 * requests complete at different times for different connections.
 * As a result, the message sent by this Phase when it starts might be
 * received by another connection before that one has completed its
 * ConnectionPhase.
 * <p>
 * For example, consider two connections A and B.
 * According to ConnectionPhase, A will establish a connection with B
 * and will then write a connection message to B.  However, before
 * the connection message is sent to B, the following steps will happen:
 * <ol>
 * <li>B receives the new connection signal</li>
 * <li>B writes a connection message to A</li>
 * <li>A receives the connection message.  Since it is the only
 * message it needs to receive, A transitions to ProcessPhase</li>
 * <li>A enters ProcessPhase and sends a process start message to B</li>
 * </ol>
 * But B is still in ConnectionPhase waiting for A to write its
 * connection message to B!  For this reason, ConnectionPhase needs to
 * know about ProcessPhase so it can forward the message.  When A
 * finally writes a connection message to B, B will process the message
 * and transition to its next phase.  If ProcessPhase was set as the
 * next phase, it would enter that phase and simply block because it
 * would not receive any more process start messages.  For this reason,
 * ConnectionPhase, when it is ready to transition, asks ProcessPhase
 * to determine the next phase. The decision will be based on whether
 * ProcessPhase has received thenecessary number of messages or not.
 * If ProcessPhase has received enough process start messages, it can
 * be skipped but must still call its enterPhase method so it notifies
 * its connections with a process start message.
 * <p>
 * This coupling is only needed between this phase and ConnectionPhase
 * because new connection requests happen in a different thread than
 * all other phase messages.
 *
 * @see ConnectionPeriod
 */
public class ProcessPeriod extends Period {
    private static final Logger logger = LoggerFactory.obtainLogger(ProcessPeriod.class);

    private final List<TalkersNetworkAddress> peers;
    private final byte[] processMsgBytes;

    public ProcessPeriod(List<TalkersNetworkAddress> peers, PeriodOverseer periodOverseer) {
        super(periodOverseer);
        this.peers = peers;

        Powerbrokermsg.BaseMessage processMsg = Powerbrokermsg.BaseMessage.newBuilder()
                .setType(Powerbrokermsg.BaseMessage.Type.PROCESS_START)
                .build();
        processMsgBytes = processMsg.toByteArray();

    }

    /**
     * Because this phase can receive messages before the phase is
     * actually entered, it is possible it has received all expected
     * messages before enterPhase is called.  In that case, the
     * enterPhase method should be called and this phase's next
     * phase returned; otherwise, just return this phase.
     *
     * @return Phase that should be entered next
     * @throws CommodityGoBetweenRaiser if there is a problem determining phase state
     */
    public Period fetchCurrentPeriod() throws CommodityGoBetweenRaiser {
        // Determine if this phase is ready to transition to the next phase.
        // If this returns null, then it still is waiting for more messages.
        // If it returns a phase, then it is ready to transition.
        Period period = shouldTransitionToNextPeriod();

        if (period != null) {
            // All expected messages have been received before this phase
            // has even been transitioned to.  Transitioning to this phase
            // would cause it to sit and never transition since it will
            // not receive any further messages.  So, act like this phase
            // has been entered and then indicate that it is ready to
            // transition as well.
            enterPeriod(); // Must ensure this Phase appears to have started

            return period;
        }

        // There are still messages that have not be received,
        // so transition to this Phase and then wait for the
        // remaining messages.
        return this;
    }

    @Override
    public void enterPeriod() throws CommodityGoBetweenRaiser {
        transmitProcessPeriodMsg();
    }

    private void transmitProcessPeriodMsg() throws CommodityGoBetweenRaiser {
        transmitFinalMessage(processMsgBytes);
    }

    @Override
    public Period handleMsg(TalkersConnection connection, Powerbrokermsg.BaseMessage msg) throws CommodityGoBetweenRaiser {
        if (msg.getType() != Powerbrokermsg.BaseMessage.Type.PROCESS_START) {
            logger.info("Invalid message type in ProcessPhase: " + msg.getType());
            return null;
        }

        addFinalMessage(connection.fetchTheirIdentity(), msg);

        return shouldTransitionToNextPeriod();
    }

    @Override
    protected Period shouldTransitionToNextPeriod() throws CommodityGoBetweenRaiser {
        if (grabNumReceivedFinalMessages() == peers.size() - 1) {
            return nextPeriod();
        }
        return null;
    }

    @Override
    protected Period nextPeriod() throws CommodityGoBetweenRaiser {
        logger.info("Moving to auction announce phase");
        return new SelloffAnnouncePeriod(obtainPeriodOverseer());
    }
}
