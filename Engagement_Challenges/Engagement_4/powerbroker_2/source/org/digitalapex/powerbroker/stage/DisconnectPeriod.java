package org.digitalapex.powerbroker.stage;


import org.digitalapex.talkers.TalkersConnection;
import org.digitalapex.talkers.TalkersRaiser;
import org.digitalapex.talkers.TalkersNetworkAddress;
import org.digitalapex.powerbroker.CommodityGoBetweenRaiser;
import org.digitalapex.powerbroker.Powerbrokermsg;
import org.digitalapex.logging.Logger;
import org.digitalapex.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DisconnectPeriod extends Period {
    private final static long TIME_TO_WAIT=1000;
    private final TalkersNetworkAddress us;
    private final List<TalkersNetworkAddress> peers;
    private boolean printedDisconnectMessage;
    private Logger logger = LoggerFactory.obtainLogger(getClass());

    public DisconnectPeriod(PeriodOverseer overseer) {
        super(overseer);
        us = overseer.takeIdentity().obtainCallbackAddress();
        peers = new ArrayList<>(overseer.obtainPeers());
        printedDisconnectMessage = false;

    }

    @Override
    public void enterPeriod() throws CommodityGoBetweenRaiser {
        PeriodOverseer periodOverseer = obtainPeriodOverseer();
        // we only want to disconnect from people after us in the list
        List<TalkersNetworkAddress> peersToDisconnect = new ArrayList<>();
        boolean disconnectFromUser = false;
        for (int j = 0; j < peers.size(); ) {
            while ((j < peers.size()) && (Math.random() < 0.6)) {
                for (; (j < peers.size()) && (Math.random() < 0.5); j++) {
                    TalkersNetworkAddress peer = peers.get(j);
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

        periodOverseer.disconnectFromUsers(peersToDisconnect);

        peersToDisconnect.add(us);
        if (peersToDisconnect.containsAll(peers)) {
            printedDisconnectMessage = true;
            periodOverseer.grabCommodityGoBetweenUser().disconnectedFromAllUsers();
        }


    }

    @Override
    public Period closedConnection(TalkersConnection connection) throws TalkersRaiser {
        if (disconnectedFromEveryone() && !printedDisconnectMessage) {
            obtainPeriodOverseer().grabCommodityGoBetweenUser().disconnectedFromAllUsers();
            printedDisconnectMessage = true;
        }
        return super.closedConnection(connection);
    }

    public boolean disconnectedFromEveryone() {
        // we should be the only peer left in this list
        return obtainPeriodOverseer().obtainPeers().size() == 1;
    }

    @Override
    public Period handleMsg(TalkersConnection connection, Powerbrokermsg.BaseMessage msg) throws CommodityGoBetweenRaiser {
        return null;
    }

    @Override
    protected Period nextPeriod() throws CommodityGoBetweenRaiser {
        return null;
    }
}
