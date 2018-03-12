package net.roboticapex.broker.period;

import net.roboticapex.senderReceivers.SenderReceiversConnection;
import net.roboticapex.senderReceivers.SenderReceiversNetworkAddress;
import net.roboticapex.broker.ProductLiaisonDeviation;
import net.roboticapex.broker.Powerbrokermsg;
import net.roboticapex.logging.Logger;
import net.roboticapex.logging.LoggerFactory;

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
 * @see ConnectionStep
 */
public class ProcessStep extends Step {
    private static final Logger logger = LoggerFactory.fetchLogger(ProcessStep.class);

    private final List<SenderReceiversNetworkAddress> peers;
    private final byte[] processMsgBytes;

    public ProcessStep(List<SenderReceiversNetworkAddress> peers, StepOverseer stepOverseer) {
        super(stepOverseer);
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
     * @throws ProductLiaisonDeviation if there is a problem determining phase state
     */
    public Step takeCurrentStep() throws ProductLiaisonDeviation {
        // Determine if this phase is ready to transition to the next phase.
        // If this returns null, then it still is waiting for more messages.
        // If it returns a phase, then it is ready to transition.
        Step step = shouldTransitionToNextStep();

        if (step != null) {
            // All expected messages have been received before this phase
            // has even been transitioned to.  Transitioning to this phase
            // would cause it to sit and never transition since it will
            // not receive any further messages.  So, act like this phase
            // has been entered and then indicate that it is ready to
            // transition as well.
            enterStep(); // Must ensure this Phase appears to have started

            return step;
        }

        // There are still messages that have not be received,
        // so transition to this Phase and then wait for the
        // remaining messages.
        return this;
    }

    @Override
    public void enterStep() throws ProductLiaisonDeviation {
        transferProcessStepMsg();
    }

    private void transferProcessStepMsg() throws ProductLiaisonDeviation {
        transferFinalMessage(processMsgBytes);
    }

    @Override
    public Step handleMsg(SenderReceiversConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductLiaisonDeviation {
        if (msg.getType() != Powerbrokermsg.BaseMessage.Type.PROCESS_START) {
            logger.info("Invalid message type in ProcessPhase: " + msg.getType());
            return null;
        }

        addFinalMessage(connection.obtainTheirIdentity(), msg);

        return shouldTransitionToNextStep();
    }

    @Override
    protected Step shouldTransitionToNextStep() throws ProductLiaisonDeviation {
        if (pullNumReceivedFinalMessages() == peers.size() - 1) {
            return nextStep();
        }
        return null;
    }

    @Override
    protected Step nextStep() throws ProductLiaisonDeviation {
        logger.info("Moving to auction announce phase");
        return new TradeAnnounceStep(grabStepOverseer());
    }
}
