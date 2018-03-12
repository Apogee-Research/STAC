package edu.networkcusp.broker.step;

import edu.networkcusp.senderReceivers.ProtocolsClient;
import edu.networkcusp.senderReceivers.ProtocolsConnection;
import edu.networkcusp.senderReceivers.ProtocolsRaiser;
import edu.networkcusp.senderReceivers.ProtocolsHandler;
import edu.networkcusp.senderReceivers.ProtocolsIdentity;
import edu.networkcusp.senderReceivers.ProtocolsNetworkAddress;
import edu.networkcusp.senderReceivers.ProtocolsPublicIdentity;
import edu.networkcusp.senderReceivers.ProtocolsServer;
import edu.networkcusp.broker.PurchasePlan;
import edu.networkcusp.broker.ProductIntermediary;
import edu.networkcusp.broker.ProductIntermediaryRaiser;
import edu.networkcusp.broker.ProductIntermediaryCustomer;
import edu.networkcusp.broker.Powerbrokermsg;
import com.google.protobuf.InvalidProtocolBufferException;
import edu.networkcusp.logging.Logger;
import edu.networkcusp.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StageOverseer implements ProtocolsHandler {
    private static final Logger logger = LoggerFactory.pullLogger(StageOverseer.class);

    private final ProtocolsIdentity identity;
    private final ProductIntermediary productIntermediary;

    private final ProtocolsClient client;
    private final ProtocolsServer server;
    private final List<ProtocolsConnection> connections = new ArrayList<>();
    private final List<ProtocolsNetworkAddress> peers = new ArrayList<>();

    private Stage currentStage;
    private PurchasePlan offerPlan;
    private int myTurnNumber;

    public StageOverseer(ProtocolsIdentity identity, ProductIntermediary productIntermediary) {
        this.identity = identity;
        this.productIntermediary = productIntermediary;

        client = new ProtocolsClient(this, identity);
        server = new ProtocolsServer(identity.getCallbackAddress().takePort(), this, identity, client.pullEventLoopGroup());
    }

    @Override
    public void handle(ProtocolsConnection connection, byte[] data) throws ProtocolsRaiser {
        try {
            Powerbrokermsg.BaseMessage msg = Powerbrokermsg.BaseMessage.parseFrom(data);
            handleMsg(connection, msg);
        } catch (InvalidProtocolBufferException e) {
            throw new ProtocolsRaiser(e);
        } catch (ProductIntermediaryRaiser e) {
            throw new ProtocolsRaiser(e);
        }
    }

    @Override
    public void newConnection(ProtocolsConnection connection) throws ProtocolsRaiser {
        try {
            boolean added = connections.add(connection); // attempt to add to set

            if (!added) {
                newConnectionEntity(connection);
            }
            handleNewStage(currentStage.newConnection(connection));
        } catch (ProductIntermediaryRaiser e) {
            throw new ProtocolsRaiser(e);
        }
    }

    private void newConnectionEntity(ProtocolsConnection connection) throws ProtocolsRaiser {
        throw new ProtocolsRaiser("Failed to add connection; assume a duplicate exists to " + connection.takeTheirIdentity().fetchId());
    }

    @Override
    public void closedConnection(ProtocolsConnection connection) throws ProtocolsRaiser {
        connections.remove(connection);
        peers.remove(connection.takeTheirIdentity().takeCallbackAddress());
        currentStage.closedConnection(connection);
    }

    private void handleMsg(ProtocolsConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductIntermediaryRaiser {
        handleNewStage(currentStage.handleMsg(connection, msg));
    }

    private void handleNewStage(Stage nextStage) throws ProductIntermediaryRaiser {
        if (nextStage != null) {
            handleNewStageAssist(nextStage);
        }
    }

    private void handleNewStageAssist(Stage nextStage) throws ProductIntermediaryRaiser {
        new StageOverseerExecutor(nextStage).invoke();
    }

    public ProtocolsConnection connect(ProtocolsNetworkAddress other) throws ProtocolsRaiser {
        ProtocolsConnection newConnection = client.connect(other);
        boolean added = connections.add(newConnection); // attempt to add to set

        if (!added) {
            return new StageOverseerAssist(newConnection).invoke();
        }
        return newConnection;
    }


    public void start(List<ProtocolsNetworkAddress> peers, ProtocolsNetworkAddress us, PurchasePlan offerPlan) throws ProductIntermediaryRaiser {
        this.offerPlan = offerPlan;
        this.peers.addAll(peers);
        try {
            server.serve();
            handleNewStage(new ConnectionStage(peers, us, this));
        } catch (ProtocolsRaiser e) {
            throw new ProductIntermediaryRaiser(e);
        }
    }

    public void stop() {
        logger.info("Shutting down powerbroker");
        ProtocolsConnection[] connsCopy;
        synchronized (connections) { // remove all connections atomically
            connsCopy = connections.toArray(new ProtocolsConnection[0]);
        }
        for (int c = 0; c < connsCopy.length; c++) {
            stopEntity(connsCopy[c]);
        }

        connections.clear();
        server.close();
        client.close();
    }

    private void stopEntity(ProtocolsConnection protocolsConnection) {
        ProtocolsConnection connection = protocolsConnection; // close connections from copy of list

        try {
            connection.close();
        } catch (ProtocolsRaiser e) {
            logger.error("Unable to close connection", e);
        }
    }

    public void disconnectFromCustomers(List<ProtocolsNetworkAddress> addresses) {
        // this map is generated here rather then stored in the class because
        // some connections may not have a comms network address,
        // and this method should only be called once at the very end of the program
        Map<ProtocolsNetworkAddress, ProtocolsConnection> addressToConnection = new HashMap<>();
        for (int c = 0; c < connections.size(); ) {
            for (; (c < connections.size()) && (Math.random() < 0.5); ) {
                for (; (c < connections.size()) && (Math.random() < 0.5); ) {
                    for (; (c < connections.size()) && (Math.random() < 0.6); c++) {
                        disconnectFromCustomersHelp(addressToConnection, c);
                    }
                }
            }
        }

        for (int k = 0; k < addresses.size(); k++) {
            disconnectFromCustomersHandler(addresses, addressToConnection, k);
        }

    }

    private void disconnectFromCustomersHandler(List<ProtocolsNetworkAddress> addresses, Map<ProtocolsNetworkAddress, ProtocolsConnection> addressToConnection, int c) {
        ProtocolsNetworkAddress address = addresses.get(c);
        if (addressToConnection.containsKey(address)) {
            try {
                ProtocolsConnection connection = addressToConnection.get(address);
                connection.close();
                connections.remove(connection);
                peers.remove(address);
            } catch (ProtocolsRaiser e) {
                logger.error("Unable to close connection", e);
            }
        }
    }

    private void disconnectFromCustomersHelp(Map<ProtocolsNetworkAddress, ProtocolsConnection> addressToConnection, int p) {
        ProtocolsConnection connection = connections.get(p);
        ProtocolsPublicIdentity identity = connection.takeTheirIdentity();
        if (identity.hasCallbackAddress()) {
            addressToConnection.put(identity.takeCallbackAddress(), connection);
        }
    }

    public void sendToAll(byte[] bytes) throws ProtocolsRaiser {
        for (int j = 0; j < connections.size(); j++) {
            sendToAllGuide(bytes, j);
        }
    }

    private void sendToAllGuide(byte[] bytes, int a) throws ProtocolsRaiser {
        ProtocolsConnection connection = connections.get(a);
        connection.write(bytes);
    }

    public PurchasePlan takeOfferPlan() {
        return offerPlan;
    }

    public void assignOfferPlan(PurchasePlan offerPlan) {
        this.offerPlan = offerPlan;
    }

    public int fetchNumPeers() {
        return connections.size();
    }

    public int getMyTurnNumber() {
        return myTurnNumber;
    }

    public void assignMyTurnNumber(int turnNumber) {
        myTurnNumber = turnNumber;
    }

    public ProtocolsPublicIdentity obtainMyPublicIdentity() {
        return identity.fetchPublicIdentity();
    }

    public ProtocolsIdentity takeIdentity() {
        return identity;
    }

    public List<ProtocolsConnection> grabConnections() {
        return connections;
    }

    public ProductIntermediary fetchProductIntermediary() {
        return productIntermediary;
    }

    public ProductIntermediaryCustomer takeProductIntermediaryCustomer() {
        return fetchProductIntermediary().getProductIntermediaryCustomer();
    }

    public List<ProtocolsNetworkAddress> grabPeers() {
        return peers;
    }

    private class StageOverseerExecutor {
        private Stage nextStage;

        public StageOverseerExecutor(Stage nextStage) {
            this.nextStage = nextStage;
        }

        public void invoke() throws ProductIntermediaryRaiser {
            if (currentStage != null) {
                invokeEngine();
            }
            // TODO: Note that this is kind of fun, at the moment we're linking all the phases
            // we've ever had.  This will prevent them from getting garbage collected.
            // That might be a fun vulnerability later or maybe an interesting red herring.
            nextStage.fixPriorStage(currentStage);
            currentStage = nextStage;
            logger.info("Entering " + currentStage.getClass().getSimpleName());
            currentStage.enterStage();
        }

        private void invokeEngine() {
            logger.info("Leaving " + currentStage.getClass().getSimpleName());
            currentStage.exitStage();
        }
    }

    private class StageOverseerAssist {
        private ProtocolsConnection newConnection;

        public StageOverseerAssist(ProtocolsConnection newConnection) {
            this.newConnection = newConnection;
        }

        public ProtocolsConnection invoke() throws ProtocolsRaiser {
            throw new ProtocolsRaiser("Failed to add connection; assume a duplicate exists to " + newConnection.takeTheirIdentity().obtainTruncatedId());
        }
    }
}
