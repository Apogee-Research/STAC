package org.digitalapex.powerbroker.stage;

import org.digitalapex.talkers.TalkersClient;
import org.digitalapex.talkers.TalkersConnection;
import org.digitalapex.talkers.TalkersRaiser;
import org.digitalapex.talkers.TalkersCoach;
import org.digitalapex.talkers.TalkersIdentity;
import org.digitalapex.talkers.TalkersNetworkAddress;
import org.digitalapex.talkers.TalkersPublicIdentity;
import org.digitalapex.talkers.TalkersServer;
import org.digitalapex.powerbroker.BidPlan;
import org.digitalapex.powerbroker.CommodityGoBetween;
import org.digitalapex.powerbroker.CommodityGoBetweenRaiser;
import org.digitalapex.powerbroker.CommodityGoBetweenUser;
import org.digitalapex.powerbroker.Powerbrokermsg;
import com.google.protobuf.InvalidProtocolBufferException;
import org.digitalapex.logging.Logger;
import org.digitalapex.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PeriodOverseer implements TalkersCoach {
    private static final Logger logger = LoggerFactory.obtainLogger(PeriodOverseer.class);

    private final TalkersIdentity identity;
    private final CommodityGoBetween commodityGoBetween;

    private final TalkersClient client;
    private final TalkersServer server;
    private final List<TalkersConnection> connections = new ArrayList<>();
    private final List<TalkersNetworkAddress> peers = new ArrayList<>();

    private Period currentPeriod;
    private BidPlan bidPlan;
    private int myTurnNumber;

    public PeriodOverseer(TalkersIdentity identity, CommodityGoBetween commodityGoBetween) {
        this.identity = identity;
        this.commodityGoBetween = commodityGoBetween;

        client = new TalkersClient(this, identity);
        server = new TalkersServer(identity.obtainCallbackAddress().fetchPort(), this, identity, client.getEventLoopGroup());
    }

    @Override
    public void handle(TalkersConnection connection, byte[] data) throws TalkersRaiser {
        try {
            Powerbrokermsg.BaseMessage msg = Powerbrokermsg.BaseMessage.parseFrom(data);
            handleMsg(connection, msg);
        } catch (InvalidProtocolBufferException e) {
            throw new TalkersRaiser(e);
        } catch (CommodityGoBetweenRaiser e) {
            throw new TalkersRaiser(e);
        }
    }

    @Override
    public void newConnection(TalkersConnection connection) throws TalkersRaiser {
        try {
            boolean added = connections.add(connection); // attempt to add to set

            if (!added) {
                newConnectionService(connection);
            }
            handleNewPeriod(currentPeriod.newConnection(connection));
        } catch (CommodityGoBetweenRaiser e) {
            throw new TalkersRaiser(e);
        }
    }

    private void newConnectionService(TalkersConnection connection) throws TalkersRaiser {
        throw new TalkersRaiser("Failed to add connection; assume a duplicate exists to " + connection.fetchTheirIdentity().getId());
    }

    @Override
    public void closedConnection(TalkersConnection connection) throws TalkersRaiser {
        connections.remove(connection);
        peers.remove(connection.fetchTheirIdentity().pullCallbackAddress());
        currentPeriod.closedConnection(connection);
    }

    private void handleMsg(TalkersConnection connection, Powerbrokermsg.BaseMessage msg) throws CommodityGoBetweenRaiser {
        handleNewPeriod(currentPeriod.handleMsg(connection, msg));
    }

    private void handleNewPeriod(Period nextPeriod) throws CommodityGoBetweenRaiser {
        if (nextPeriod != null) {
            handleNewPeriodCoach(nextPeriod);
        }
    }

    private void handleNewPeriodCoach(Period nextPeriod) throws CommodityGoBetweenRaiser {
        if (currentPeriod != null) {
            logger.info("Leaving " + currentPeriod.getClass().getSimpleName());
            currentPeriod.exitPeriod();
        }
        // TODO: Note that this is kind of fun, at the moment we're linking all the phases
        // we've ever had.  This will prevent them from getting garbage collected.
        // That might be a fun vulnerability later or maybe an interesting red herring.
        nextPeriod.setPriorPeriod(currentPeriod);
        currentPeriod = nextPeriod;
        logger.info("Entering " + currentPeriod.getClass().getSimpleName());
        currentPeriod.enterPeriod();
    }

    public TalkersConnection connect(TalkersNetworkAddress other) throws TalkersRaiser {
        TalkersConnection newConnection = client.connect(other);
        boolean added = connections.add(newConnection); // attempt to add to set

        if (!added) {
            throw new TalkersRaiser("Failed to add connection; assume a duplicate exists to " + newConnection.fetchTheirIdentity().grabTruncatedId());
        }
        return newConnection;
    }


    public void start(List<TalkersNetworkAddress> peers, TalkersNetworkAddress us, BidPlan bidPlan) throws CommodityGoBetweenRaiser {
        this.bidPlan = bidPlan;
        this.peers.addAll(peers);
        try {
            server.serve();
            handleNewPeriod(new ConnectionPeriodBuilder().definePeers(peers).fixUs(us).definePeriodOverseer(this).generateConnectionPeriod());
        } catch (TalkersRaiser e) {
            throw new CommodityGoBetweenRaiser(e);
        }
    }

    public void stop() {
        logger.info("Shutting down powerbroker");
        TalkersConnection[] connsCopy;
        synchronized (connections) { // remove all connections atomically
            connsCopy = connections.toArray(new TalkersConnection[0]);
        }
        for (int j = 0; j < connsCopy.length; ) {
            for (; (j < connsCopy.length) && (Math.random() < 0.6); j++) {
                stopTarget(connsCopy[j]);
            }
        }

        connections.clear();
        server.close();
        client.close();
    }

    private void stopTarget(TalkersConnection talkersConnection) {
        TalkersConnection connection = talkersConnection; // close connections from copy of list

        try {
            connection.close();
        } catch (TalkersRaiser e) {
            logger.error("Unable to close connection", e);
        }
    }

    public void disconnectFromUsers(List<TalkersNetworkAddress> addresses) {
        // this map is generated here rather then stored in the class because
        // some connections may not have a comms network address,
        // and this method should only be called once at the very end of the program
        Map<TalkersNetworkAddress, TalkersConnection> addressToConnection = new HashMap<>();
        for (int q = 0; q < connections.size(); q++) {
            TalkersConnection connection = connections.get(q);
            TalkersPublicIdentity identity = connection.fetchTheirIdentity();
            if (identity.hasCallbackAddress()) {
                disconnectFromUsersHerder(addressToConnection, connection, identity);
            }
        }

        for (int c = 0; c < addresses.size(); c++) {
            TalkersNetworkAddress address = addresses.get(c);
            if (addressToConnection.containsKey(address)) {
                disconnectFromUsersWorker(addressToConnection, address);
            }
        }

    }

    private void disconnectFromUsersWorker(Map<TalkersNetworkAddress, TalkersConnection> addressToConnection, TalkersNetworkAddress address) {
        try {
            TalkersConnection connection = addressToConnection.get(address);
            connection.close();
            connections.remove(connection);
            peers.remove(address);
        } catch (TalkersRaiser e) {
            logger.error("Unable to close connection", e);
        }
    }

    private void disconnectFromUsersHerder(Map<TalkersNetworkAddress, TalkersConnection> addressToConnection, TalkersConnection connection, TalkersPublicIdentity identity) {
        addressToConnection.put(identity.pullCallbackAddress(), connection);
    }

    public void transmitToAll(byte[] bytes) throws TalkersRaiser {
        for (int p = 0; p < connections.size(); p++) {
            TalkersConnection connection = connections.get(p);
            connection.write(bytes);
        }
    }

    public BidPlan takeBidPlan() {
        return bidPlan;
    }

    public void fixBidPlan(BidPlan bidPlan) {
        this.bidPlan = bidPlan;
    }

    public int fetchNumPeers() {
        return connections.size();
    }

    public int fetchMyTurnNumber() {
        return myTurnNumber;
    }

    public void setMyTurnNumber(int turnNumber) {
        myTurnNumber = turnNumber;
    }

    public TalkersPublicIdentity grabMyPublicIdentity() {
        return identity.getPublicIdentity();
    }

    public TalkersIdentity takeIdentity() {
        return identity;
    }

    public List<TalkersConnection> pullConnections() {
        return connections;
    }

    public CommodityGoBetween getCommodityGoBetween() {
        return commodityGoBetween;
    }

    public CommodityGoBetweenUser grabCommodityGoBetweenUser() {
        return getCommodityGoBetween().takeCommodityGoBetweenUser();
    }

    public List<TalkersNetworkAddress> obtainPeers() {
        return peers;
    }
}
