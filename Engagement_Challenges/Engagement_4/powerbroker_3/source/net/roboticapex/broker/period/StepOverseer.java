package net.roboticapex.broker.period;

import net.roboticapex.senderReceivers.SenderReceiversClient;
import net.roboticapex.senderReceivers.SenderReceiversConnection;
import net.roboticapex.senderReceivers.SenderReceiversDeviation;
import net.roboticapex.senderReceivers.SenderReceiversHandler;
import net.roboticapex.senderReceivers.SenderReceiversIdentity;
import net.roboticapex.senderReceivers.SenderReceiversNetworkAddress;
import net.roboticapex.senderReceivers.SenderReceiversPublicIdentity;
import net.roboticapex.senderReceivers.SenderReceiversServer;
import net.roboticapex.broker.BidPlan;
import net.roboticapex.broker.ProductLiaison;
import net.roboticapex.broker.ProductLiaisonDeviation;
import net.roboticapex.broker.ProductLiaisonUser;
import net.roboticapex.broker.Powerbrokermsg;
import com.google.protobuf.InvalidProtocolBufferException;
import net.roboticapex.logging.Logger;
import net.roboticapex.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StepOverseer implements SenderReceiversHandler {
    private static final Logger logger = LoggerFactory.fetchLogger(StepOverseer.class);

    private final SenderReceiversIdentity identity;
    private final ProductLiaison productLiaison;

    private final SenderReceiversClient client;
    private final SenderReceiversServer server;
    private final List<SenderReceiversConnection> connections = new ArrayList<>();
    private final List<SenderReceiversNetworkAddress> peers = new ArrayList<>();

    private Step currentStep;
    private BidPlan promisePlan;
    private int myTurnNumber;

    public StepOverseer(SenderReceiversIdentity identity, ProductLiaison productLiaison) {
        this.identity = identity;
        this.productLiaison = productLiaison;

        client = new SenderReceiversClient(this, identity);
        server = new SenderReceiversServer(identity.getCallbackAddress().pullPort(), this, identity, client.getEventLoopGroup());
    }

    @Override
    public void handle(SenderReceiversConnection connection, byte[] data) throws SenderReceiversDeviation {
        try {
            Powerbrokermsg.BaseMessage msg = Powerbrokermsg.BaseMessage.parseFrom(data);
            handleMsg(connection, msg);
        } catch (InvalidProtocolBufferException e) {
            throw new SenderReceiversDeviation(e);
        } catch (ProductLiaisonDeviation e) {
            throw new SenderReceiversDeviation(e);
        }
    }

    @Override
    public void newConnection(SenderReceiversConnection connection) throws SenderReceiversDeviation {
        try {
            boolean added = connections.add(connection); // attempt to add to set

            if (!added) {
                throw new SenderReceiversDeviation("Failed to add connection; assume a duplicate exists to " + connection.obtainTheirIdentity().obtainId());
            }
            handleNewStep(currentStep.newConnection(connection));
        } catch (ProductLiaisonDeviation e) {
            throw new SenderReceiversDeviation(e);
        }
    }

    @Override
    public void closedConnection(SenderReceiversConnection connection) throws SenderReceiversDeviation {
        connections.remove(connection);
        peers.remove(connection.obtainTheirIdentity().pullCallbackAddress());
        currentStep.closedConnection(connection);
    }

    private void handleMsg(SenderReceiversConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductLiaisonDeviation {
        handleNewStep(currentStep.handleMsg(connection, msg));
    }

    private void handleNewStep(Step nextStep) throws ProductLiaisonDeviation {
        if (nextStep != null) {
            handleNewStepHandler(nextStep);
        }
    }

    private void handleNewStepHandler(Step nextStep) throws ProductLiaisonDeviation {
        if (currentStep != null) {
            handleNewStepHandlerGuide();
        }
        // TODO: Note that this is kind of fun, at the moment we're linking all the phases
        // we've ever had.  This will prevent them from getting garbage collected.
        // That might be a fun vulnerability later or maybe an interesting red herring.
        nextStep.fixPriorStep(currentStep);
        currentStep = nextStep;
        logger.info("Entering " + currentStep.getClass().getSimpleName());
        currentStep.enterStep();
    }

    private void handleNewStepHandlerGuide() {
        logger.info("Leaving " + currentStep.getClass().getSimpleName());
        currentStep.exitStep();
    }

    public SenderReceiversConnection connect(SenderReceiversNetworkAddress other) throws SenderReceiversDeviation {
        SenderReceiversConnection newConnection = client.connect(other);
        boolean added = connections.add(newConnection); // attempt to add to set

        if (!added) {
            return connectTarget(newConnection);
        }
        return newConnection;
    }

    private SenderReceiversConnection connectTarget(SenderReceiversConnection newConnection) throws SenderReceiversDeviation {
        throw new SenderReceiversDeviation("Failed to add connection; assume a duplicate exists to " + newConnection.obtainTheirIdentity().pullTruncatedId());
    }


    public void start(List<SenderReceiversNetworkAddress> peers, SenderReceiversNetworkAddress us, BidPlan promisePlan) throws ProductLiaisonDeviation {
        this.promisePlan = promisePlan;
        this.peers.addAll(peers);
        try {
            server.serve();
            handleNewStep(new ConnectionStep(peers, us, this));
        } catch (SenderReceiversDeviation e) {
            throw new ProductLiaisonDeviation(e);
        }
    }

    public void stop() {
        logger.info("Shutting down powerbroker");
        SenderReceiversConnection[] connsCopy;
        synchronized (connections) { // remove all connections atomically
            connsCopy = connections.toArray(new SenderReceiversConnection[0]);
        }
        for (int q = 0; q < connsCopy.length; q++) {
            new StepOverseerEngine(connsCopy[q]).invoke();
        }

        connections.clear();
        server.close();
        client.close();
    }

    public void disconnectFromUsers(List<SenderReceiversNetworkAddress> addresses) {
        // this map is generated here rather then stored in the class because
        // some connections may not have a comms network address,
        // and this method should only be called once at the very end of the program
        Map<SenderReceiversNetworkAddress, SenderReceiversConnection> addressToConnection = new HashMap<>();
        for (int j = 0; j < connections.size(); j++) {
            SenderReceiversConnection connection = connections.get(j);
            SenderReceiversPublicIdentity identity = connection.obtainTheirIdentity();
            if (identity.hasCallbackAddress()) {
                disconnectFromUsersHerder(addressToConnection, connection, identity);
            }
        }

        for (int a = 0; a < addresses.size(); ) {
            for (; (a < addresses.size()) && (Math.random() < 0.4); a++) {
                disconnectFromUsersTarget(addresses, addressToConnection, a);
            }
        }

    }

    private void disconnectFromUsersTarget(List<SenderReceiversNetworkAddress> addresses, Map<SenderReceiversNetworkAddress, SenderReceiversConnection> addressToConnection, int b) {
        SenderReceiversNetworkAddress address = addresses.get(b);
        if (addressToConnection.containsKey(address)) {
            disconnectFromUsersTargetService(addressToConnection, address);
        }
    }

    private void disconnectFromUsersTargetService(Map<SenderReceiversNetworkAddress, SenderReceiversConnection> addressToConnection, SenderReceiversNetworkAddress address) {
        try {
            SenderReceiversConnection connection = addressToConnection.get(address);
            connection.close();
            connections.remove(connection);
            peers.remove(address);
        } catch (SenderReceiversDeviation e) {
            logger.error("Unable to close connection", e);
        }
    }

    private void disconnectFromUsersHerder(Map<SenderReceiversNetworkAddress, SenderReceiversConnection> addressToConnection, SenderReceiversConnection connection, SenderReceiversPublicIdentity identity) {
        addressToConnection.put(identity.pullCallbackAddress(), connection);
    }

    public void transferToAll(byte[] bytes) throws SenderReceiversDeviation {
        for (int p = 0; p < connections.size(); p++) {
            transferToAllHome(bytes, p);
        }
    }

    private void transferToAllHome(byte[] bytes, int b) throws SenderReceiversDeviation {
        SenderReceiversConnection connection = connections.get(b);
        connection.write(bytes);
    }

    public BidPlan getPromisePlan() {
        return promisePlan;
    }

    public void assignPromisePlan(BidPlan promisePlan) {
        this.promisePlan = promisePlan;
    }

    public int grabNumPeers() {
        return connections.size();
    }

    public int takeMyTurnNumber() {
        return myTurnNumber;
    }

    public void assignMyTurnNumber(int turnNumber) {
        myTurnNumber = turnNumber;
    }

    public SenderReceiversPublicIdentity takeMyPublicIdentity() {
        return identity.pullPublicIdentity();
    }

    public SenderReceiversIdentity fetchIdentity() {
        return identity;
    }

    public List<SenderReceiversConnection> obtainConnections() {
        return connections;
    }

    public ProductLiaison pullProductLiaison() {
        return productLiaison;
    }

    public ProductLiaisonUser fetchProductLiaisonUser() {
        return pullProductLiaison().obtainProductLiaisonUser();
    }

    public List<SenderReceiversNetworkAddress> getPeers() {
        return peers;
    }

    private class StepOverseerEngine {
        private SenderReceiversConnection senderReceiversConnection;

        public StepOverseerEngine(SenderReceiversConnection senderReceiversConnection) {
            this.senderReceiversConnection = senderReceiversConnection;
        }

        public void invoke() {
            SenderReceiversConnection connection = senderReceiversConnection; // close connections from copy of list

            try {
                connection.close();
            } catch (SenderReceiversDeviation e) {
                logger.error("Unable to close connection", e);
            }
        }
    }
}
