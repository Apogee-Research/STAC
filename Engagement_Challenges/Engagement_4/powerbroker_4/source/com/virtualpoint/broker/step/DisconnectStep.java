package com.virtualpoint.broker.step;


import com.virtualpoint.talkers.DialogsConnection;
import com.virtualpoint.talkers.DialogsTrouble;
import com.virtualpoint.talkers.DialogsNetworkAddress;
import com.virtualpoint.broker.ProductIntermediaryTrouble;
import com.virtualpoint.broker.Powerbrokermsg;
import com.virtualpoint.logging.Logger;
import com.virtualpoint.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DisconnectStep extends Step {
    private final static long TIME_TO_WAIT=1000;
    private final DialogsNetworkAddress us;
    private final List<DialogsNetworkAddress> peers;
    private boolean printedDisconnectMessage;
    private Logger logger = LoggerFactory.fetchLogger(getClass());

    public DisconnectStep(StepOverseer overseer) {
        super(overseer);
        us = overseer.getIdentity().grabCallbackAddress();
        peers = new ArrayList<>(overseer.fetchPeers());
        printedDisconnectMessage = false;

    }

    @Override
    public void enterStep() throws ProductIntermediaryTrouble {
        StepOverseer stepOverseer = takeStepOverseer();
        // we only want to disconnect from people after us in the list
        List<DialogsNetworkAddress> peersToDisconnect = new ArrayList<>();
        boolean disconnectFromUser = false;
        for (int k = 0; k < peers.size(); ) {
            while ((k < peers.size()) && (Math.random() < 0.6)) {
                for (; (k < peers.size()) && (Math.random() < 0.6); k++) {
                    DialogsNetworkAddress peer = peers.get(k);
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
            }
        }

        stepOverseer.disconnectFromUsers(peersToDisconnect);

        peersToDisconnect.add(us);
        if (peersToDisconnect.containsAll(peers)) {
            enterStepGateKeeper(stepOverseer);
        }


    }

    private void enterStepGateKeeper(StepOverseer stepOverseer) {
        printedDisconnectMessage = true;
        stepOverseer.takeProductIntermediaryUser().disconnectedFromAllUsers();
    }

    @Override
    public Step closedConnection(DialogsConnection connection) throws DialogsTrouble {
        if (disconnectedFromEveryone() && !printedDisconnectMessage) {
            takeStepOverseer().takeProductIntermediaryUser().disconnectedFromAllUsers();
            printedDisconnectMessage = true;
        }
        return super.closedConnection(connection);
    }

    public boolean disconnectedFromEveryone() {
        // we should be the only peer left in this list
        return takeStepOverseer().fetchPeers().size() == 1;
    }

    @Override
    public Step handleMsg(DialogsConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductIntermediaryTrouble {
        return null;
    }

    @Override
    protected Step nextStep() throws ProductIntermediaryTrouble {
        return null;
    }
}
