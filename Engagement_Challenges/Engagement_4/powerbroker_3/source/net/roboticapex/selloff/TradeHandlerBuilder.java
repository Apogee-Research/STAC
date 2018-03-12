package net.roboticapex.selloff;

import net.roboticapex.selloff.messagedata.TradeSerializer;
import net.roboticapex.senderReceivers.SenderReceiversIdentity;
import net.roboticapex.senderReceivers.Communicator;

public class TradeHandlerBuilder {
    private int port;
    private SenderReceiversIdentity identity;
    private Communicator communicator;
    private TradeSerializer serializer;
    private TradeOperator operator;
    private int maxPromise;
    private TradeUserAPI userAPI;

    public TradeHandlerBuilder assignPort(int port) {
        this.port = port;
        return this;
    }

    public TradeHandlerBuilder setIdentity(SenderReceiversIdentity identity) {
        this.identity = identity;
        return this;
    }

    public TradeHandlerBuilder assignCommunicator(Communicator communicator) {
        this.communicator = communicator;
        return this;
    }

    public TradeHandlerBuilder assignSerializer(TradeSerializer serializer) {
        this.serializer = serializer;
        return this;
    }

    public TradeHandlerBuilder assignOperator(TradeOperator operator) {
        this.operator = operator;
        return this;
    }

    public TradeHandlerBuilder assignMaxPromise(int maxPromise) {
        this.maxPromise = maxPromise;
        return this;
    }

    public TradeHandlerBuilder defineUserAPI(TradeUserAPI userAPI) {
        this.userAPI = userAPI;
        return this;
    }

    public TradeHandler makeTradeHandler() {
        return new TradeHandler(operator, communicator, userAPI, serializer, port, identity, maxPromise);
    }
}