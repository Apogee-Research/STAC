package edu.computerapex.buyOp;

import edu.computerapex.buyOp.messagedata.BarterSerializer;
import edu.computerapex.dialogs.CommunicationsIdentity;
import edu.computerapex.dialogs.Communicator;

public class BarterHandlerBuilder {
    private int port;
    private CommunicationsIdentity identity;
    private Communicator communicator;
    private BarterSerializer serializer;
    private BarterDriver driver;
    private int maxBid;
    private BarterParticipantAPI participantAPI;

    public BarterHandlerBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public BarterHandlerBuilder defineIdentity(CommunicationsIdentity identity) {
        this.identity = identity;
        return this;
    }

    public BarterHandlerBuilder setCommunicator(Communicator communicator) {
        this.communicator = communicator;
        return this;
    }

    public BarterHandlerBuilder fixSerializer(BarterSerializer serializer) {
        this.serializer = serializer;
        return this;
    }

    public BarterHandlerBuilder fixDriver(BarterDriver driver) {
        this.driver = driver;
        return this;
    }

    public BarterHandlerBuilder setMaxBid(int maxBid) {
        this.maxBid = maxBid;
        return this;
    }

    public BarterHandlerBuilder setParticipantAPI(BarterParticipantAPI participantAPI) {
        this.participantAPI = participantAPI;
        return this;
    }

    public BarterHandler generateBarterHandler() {
        return new BarterHandler(driver, communicator, participantAPI, serializer, port, identity, maxBid);
    }
}