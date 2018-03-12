package org.digitalapex.powerbroker.stage;

import org.digitalapex.talkers.TalkersNetworkAddress;

import java.util.List;

public class ConnectionPeriodBuilder {
    private List<TalkersNetworkAddress> peers;
    private TalkersNetworkAddress us;
    private PeriodOverseer periodOverseer;

    public ConnectionPeriodBuilder definePeers(List<TalkersNetworkAddress> peers) {
        this.peers = peers;
        return this;
    }

    public ConnectionPeriodBuilder fixUs(TalkersNetworkAddress us) {
        this.us = us;
        return this;
    }

    public ConnectionPeriodBuilder definePeriodOverseer(PeriodOverseer periodOverseer) {
        this.periodOverseer = periodOverseer;
        return this;
    }

    public ConnectionPeriod generateConnectionPeriod() {
        return new ConnectionPeriod(peers, us, periodOverseer);
    }
}