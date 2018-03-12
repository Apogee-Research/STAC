package edu.networkcusp.broker.step;

import edu.networkcusp.senderReceivers.ProtocolsConnection;
import edu.networkcusp.senderReceivers.ProtocolsNetworkAddress;
import edu.networkcusp.broker.ProductIntermediaryRaiser;
import edu.networkcusp.broker.Powerbrokermsg;
import edu.networkcusp.logging.Logger;
import edu.networkcusp.logging.LoggerFactory;

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
 * @see ConnectionStage
 */
public class ProcessStage extends Stage {
    private static final Logger logger = LoggerFactory.pullLogger(ProcessStage.class);

    private final List<ProtocolsNetworkAddress> peers;
    private final byte[] processMsgBytes;

    public ProcessStage(List<ProtocolsNetworkAddress> peers, StageOverseer stageOverseer) {
        super(stageOverseer);
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
     * @throws ProductIntermediaryRaiser if there is a problem determining phase state
     */
    public Stage fetchCurrentStage() throws ProductIntermediaryRaiser {
        // Determine if this phase is ready to transition to the next phase.
        // If this returns null, then it still is waiting for more messages.
        // If it returns a phase, then it is ready to transition.
        Stage stage = shouldTransitionToNextStage();

        if (stage != null) {
            // All expected messages have been received before this phase
            // has even been transitioned to.  Transitioning to this phase
            // would cause it to sit and never transition since it will
            // not receive any further messages.  So, act like this phase
            // has been entered and then indicate that it is ready to
            // transition as well.
            return grabCurrentStageWorker(stage);
        }

        // There are still messages that have not be received,
        // so transition to this Phase and then wait for the
        // remaining messages.
        return this;
    }

    private Stage grabCurrentStageWorker(Stage stage) throws ProductIntermediaryRaiser {
        enterStage(); // Must ensure this Phase appears to have started

        return stage;
    }

    @Override
    public void enterStage() throws ProductIntermediaryRaiser {
        sendProcessStageMsg();
    }

    private void sendProcessStageMsg() throws ProductIntermediaryRaiser {
        sendFinalMessage(processMsgBytes);
    }

    @Override
    public Stage handleMsg(ProtocolsConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductIntermediaryRaiser {
        if (msg.getType() != Powerbrokermsg.BaseMessage.Type.PROCESS_START) {
            return new ProcessStageCoordinator(msg).invoke();
        }

        addFinalMessage(connection.takeTheirIdentity(), msg);

        return shouldTransitionToNextStage();
    }

    @Override
    protected Stage shouldTransitionToNextStage() throws ProductIntermediaryRaiser {
        if (pullNumReceivedFinalMessages() == peers.size() - 1) {
            return nextStage();
        }
        return null;
    }

    @Override
    protected Stage nextStage() throws ProductIntermediaryRaiser {
        logger.info("Moving to auction announce phase");
        return new AuctionAnnounceStage(takeStageOverseer());
    }

    private class ProcessStageCoordinator {
        private Powerbrokermsg.BaseMessage msg;

        public ProcessStageCoordinator(Powerbrokermsg.BaseMessage msg) {
            this.msg = msg;
        }

        public Stage invoke() {
            logger.info("Invalid message type in ProcessPhase: " + msg.getType());
            return null;
        }
    }
}
