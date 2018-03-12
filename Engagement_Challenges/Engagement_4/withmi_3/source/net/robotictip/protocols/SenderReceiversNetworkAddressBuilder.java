package net.robotictip.protocols;

public class SenderReceiversNetworkAddressBuilder {
    private int port;
    private String home;

    public SenderReceiversNetworkAddressBuilder definePort(int port) {
        this.port = port;
        return this;
    }

    public SenderReceiversNetworkAddressBuilder assignHome(String home) {
        this.home = home;
        return this;
    }

    public SenderReceiversNetworkAddress generateSenderReceiversNetworkAddress() {
        return new SenderReceiversNetworkAddress(home, port);
    }
}