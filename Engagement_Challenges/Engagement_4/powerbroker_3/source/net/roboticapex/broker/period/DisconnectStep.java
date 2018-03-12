package net.roboticapex.broker.period;


import net.roboticapex.senderReceivers.SenderReceiversConnection;
import net.roboticapex.senderReceivers.SenderReceiversDeviation;
import net.roboticapex.senderReceivers.SenderReceiversNetworkAddress;
import net.roboticapex.broker.ProductLiaisonDeviation;
import net.roboticapex.broker.Powerbrokermsg;
import net.roboticapex.logging.Logger;
import net.roboticapex.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DisconnectStep extends Step {
    private final static long TIME_TO_WAIT=1000;
    private final SenderReceiversNetworkAddress us;
    private final List<SenderReceiversNetworkAddress> peers;
    private boolean printedDisconnectMessage;
    private Logger logger = LoggerFactory.fetchLogger(getClass());

    public DisconnectStep(StepOverseer overseer) {
        super(overseer);
        us = overseer.fetchIdentity().getCallbackAddress();
        peers = new ArrayList<>(overseer.getPeers());
        printedDisconnectMessage = false;

    }

    @Override
    public void enterStep() throws ProductLiaisonDeviation {
        StepOverseer stepOverseer = grabStepOverseer();
        // we only want to disconnect from people after us in the list
        List<SenderReceiversNetworkAddress> peersToDisconnect = new ArrayList<>();
        boolean disconnectFromUser = false;
        for (int k = 0; k < peers.size(); k++) {
            SenderReceiversNetworkAddress peer = peers.get(k);
            if (disconnectFromUser) {
                String peerString = peer.toString();
                if (peerString.length() > 25) {
                    peerString = peerString.substring(0, 25) + "...";
                }
                logger.info("Disconnect from " + peerString);
                peersToDisconnect.add(peer);
            } else if (peer.equals(us)) {
                logger.info("We can now start storing users we want to disconnect from");
                disconnectFromUser = true;
            }
        }

        stepOverseer.disconnectFromUsers(peersToDisconnect);

        peersToDisconnect.add(us);
        if (peersToDisconnect.containsAll(peers)) {
            printedDisconnectMessage = true;
            stepOverseer.fetchProductLiaisonUser().disconnectedFromAllUsers();
        }


    }

    @Override
    public Step closedConnection(SenderReceiversConnection connection) throws SenderReceiversDeviation {
        if (disconnectedFromEveryone() && !printedDisconnectMessage) {
            grabStepOverseer().fetchProductLiaisonUser().disconnectedFromAllUsers();
            printedDisconnectMessage = true;
        }
        return super.closedConnection(connection);
    }

    public boolean disconnectedFromEveryone() {
        // we should be the only peer left in this list
        return grabStepOverseer().getPeers().size() == 1;
    }

    @Override
    public Step handleMsg(SenderReceiversConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductLiaisonDeviation {
        return null;
    }

    @Override
    protected Step nextStep() throws ProductLiaisonDeviation {
        return null;
    }
}
