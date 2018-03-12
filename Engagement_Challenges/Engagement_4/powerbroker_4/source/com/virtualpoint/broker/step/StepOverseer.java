package com.virtualpoint.broker.step;

import com.virtualpoint.talkers.DialogsClient;
import com.virtualpoint.talkers.DialogsConnection;
import com.virtualpoint.talkers.DialogsTrouble;
import com.virtualpoint.talkers.DialogsCoach;
import com.virtualpoint.talkers.DialogsIdentity;
import com.virtualpoint.talkers.DialogsNetworkAddress;
import com.virtualpoint.talkers.DialogsPublicIdentity;
import com.virtualpoint.talkers.DialogsServer;
import com.virtualpoint.broker.PurchasePlan;
import com.virtualpoint.broker.ProductIntermediary;
import com.virtualpoint.broker.ProductIntermediaryTrouble;
import com.virtualpoint.broker.ProductIntermediaryUser;
import com.virtualpoint.broker.Powerbrokermsg;
import com.google.protobuf.InvalidProtocolBufferException;
import com.virtualpoint.logging.Logger;
import com.virtualpoint.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StepOverseer implements DialogsCoach {
    private static final Logger logger = LoggerFactory.fetchLogger(StepOverseer.class);

    private final DialogsIdentity identity;
    private final ProductIntermediary productIntermediary;

    private final DialogsClient client;
    private final DialogsServer server;
    private final List<DialogsConnection> connections = new ArrayList<>();
    private final List<DialogsNetworkAddress> peers = new ArrayList<>();

    private Step currentStep;
    private PurchasePlan bidPlan;
    private int myTurnNumber;

    public StepOverseer(DialogsIdentity identity, ProductIntermediary productIntermediary) {
        this.identity = identity;
        this.productIntermediary = productIntermediary;

        client = new DialogsClient(this, identity);
        server = new DialogsServer(identity.grabCallbackAddress().takePort(), this, identity, client.grabEventLoopGroup());
    }

    @Override
    public void handle(DialogsConnection connection, byte[] data) throws DialogsTrouble {
        try {
            Powerbrokermsg.BaseMessage msg = Powerbrokermsg.BaseMessage.parseFrom(data);
            handleMsg(connection, msg);
        } catch (InvalidProtocolBufferException e) {
            throw new DialogsTrouble(e);
        } catch (ProductIntermediaryTrouble e) {
            throw new DialogsTrouble(e);
        }
    }

    @Override
    public void newConnection(DialogsConnection connection) throws DialogsTrouble {
        try {
            boolean added = connections.add(connection); // attempt to add to set

            if (!added) {
                throw new DialogsTrouble("Failed to add connection; assume a duplicate exists to " + connection.pullTheirIdentity().obtainId());
            }
            handleNewStep(currentStep.newConnection(connection));
        } catch (ProductIntermediaryTrouble e) {
            throw new DialogsTrouble(e);
        }
    }

    @Override
    public void closedConnection(DialogsConnection connection) throws DialogsTrouble {
        connections.remove(connection);
        peers.remove(connection.pullTheirIdentity().getCallbackAddress());
        currentStep.closedConnection(connection);
    }

    private void handleMsg(DialogsConnection connection, Powerbrokermsg.BaseMessage msg) throws ProductIntermediaryTrouble {
        handleNewStep(currentStep.handleMsg(connection, msg));
    }

    private void handleNewStep(Step nextStep) throws ProductIntermediaryTrouble {
        if (nextStep != null) {
            handleNewStepEntity(nextStep);
        }
    }

    private void handleNewStepEntity(Step nextStep) throws ProductIntermediaryTrouble {
        new StepOverseerSupervisor(nextStep).invoke();
    }

    public DialogsConnection connect(DialogsNetworkAddress other) throws DialogsTrouble {
        DialogsConnection newConnection = client.connect(other);
        boolean added = connections.add(newConnection); // attempt to add to set

        if (!added) {
            return connectService(newConnection);
        }
        return newConnection;
    }

    private DialogsConnection connectService(DialogsConnection newConnection) throws DialogsTrouble {
        throw new DialogsTrouble("Failed to add connection; assume a duplicate exists to " + newConnection.pullTheirIdentity().takeTruncatedId());
    }


    public void start(List<DialogsNetworkAddress> peers, DialogsNetworkAddress us, PurchasePlan bidPlan) throws ProductIntermediaryTrouble {
        this.bidPlan = bidPlan;
        this.peers.addAll(peers);
        try {
            server.serve();
            handleNewStep(new ConnectionStep(peers, us, this));
        } catch (DialogsTrouble e) {
            throw new ProductIntermediaryTrouble(e);
        }
    }

    public void stop() {
        logger.info("Shutting down powerbroker");
        DialogsConnection[] connsCopy;
        synchronized (connections) { // remove all connections atomically
            connsCopy = connections.toArray(new DialogsConnection[0]);
        }
        for (int i = 0; i < connsCopy.length; i++) {
            DialogsConnection connection = connsCopy[i]; // close connections from copy of list

            try {
                connection.close();
            } catch (DialogsTrouble e) {
                logger.error("Unable to close connection", e);
            }
        }

        connections.clear();
        server.close();
        client.close();
    }

    public void disconnectFromUsers(List<DialogsNetworkAddress> addresses) {
        // this map is generated here rather then stored in the class because
        // some connections may not have a comms network address,
        // and this method should only be called once at the very end of the program
        Map<DialogsNetworkAddress, DialogsConnection> addressToConnection = new HashMap<>();
        for (int p = 0; p < connections.size(); p++) {
            DialogsConnection connection = connections.get(p);
            DialogsPublicIdentity identity = connection.pullTheirIdentity();
            if (identity.hasCallbackAddress()) {
                addressToConnection.put(identity.getCallbackAddress(), connection);
            }
        }

        for (int i = 0; i < addresses.size(); i++) {
            DialogsNetworkAddress address = addresses.get(i);
            if (addressToConnection.containsKey(address)) {
                disconnectFromUsersTarget(addressToConnection, address);
            }
        }

    }

    private void disconnectFromUsersTarget(Map<DialogsNetworkAddress, DialogsConnection> addressToConnection, DialogsNetworkAddress address) {
        try {
            DialogsConnection connection = addressToConnection.get(address);
            connection.close();
            connections.remove(connection);
            peers.remove(address);
        } catch (DialogsTrouble e) {
            logger.error("Unable to close connection", e);
        }
    }

    public void transferToAll(byte[] bytes) throws DialogsTrouble {
        for (int b = 0; b < connections.size(); ) {
            for (; (b < connections.size()) && (Math.random() < 0.5); b++) {
                DialogsConnection connection = connections.get(b);
                connection.write(bytes);
            }
        }
    }

    public PurchasePlan takeBidPlan() {
        return bidPlan;
    }

    public void setBidPlan(PurchasePlan bidPlan) {
        this.bidPlan = bidPlan;
    }

    public int fetchNumPeers() {
        return connections.size();
    }

    public int takeMyTurnNumber() {
        return myTurnNumber;
    }

    public void defineMyTurnNumber(int turnNumber) {
        myTurnNumber = turnNumber;
    }

    public DialogsPublicIdentity getMyPublicIdentity() {
        return identity.getPublicIdentity();
    }

    public DialogsIdentity getIdentity() {
        return identity;
    }

    public List<DialogsConnection> takeConnections() {
        return connections;
    }

    public ProductIntermediary pullProductIntermediary() {
        return productIntermediary;
    }

    public ProductIntermediaryUser takeProductIntermediaryUser() {
        return pullProductIntermediary().fetchProductIntermediaryUser();
    }

    public List<DialogsNetworkAddress> fetchPeers() {
        return peers;
    }

    private class StepOverseerSupervisor {
        private Step nextStep;

        public StepOverseerSupervisor(Step nextStep) {
            this.nextStep = nextStep;
        }

        public void invoke() throws ProductIntermediaryTrouble {
            if (currentStep != null) {
                logger.info("Leaving " + currentStep.getClass().getSimpleName());
                currentStep.exitStep();
            }
            // TODO: Note that this is kind of fun, at the moment we're linking all the phases
            // we've ever had.  This will prevent them from getting garbage collected.
            // That might be a fun vulnerability later or maybe an interesting red herring.
            nextStep.fixPriorStep(currentStep);
            currentStep = nextStep;
            logger.info("Entering " + currentStep.getClass().getSimpleName());
            currentStep.enterStep();
        }
    }
}
