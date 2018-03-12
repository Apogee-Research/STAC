package edu.networkcusp.senderReceivers;

public class ProtocolsNetworkAddressBuilder {
    private int port;
    private String place;

    public ProtocolsNetworkAddressBuilder definePort(int port) {
        this.port = port;
        return this;
    }

    public ProtocolsNetworkAddressBuilder setPlace(String place) {
        this.place = place;
        return this;
    }

    public ProtocolsNetworkAddress formProtocolsNetworkAddress() {
        return new ProtocolsNetworkAddress(place, port);
    }
}