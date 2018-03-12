package edu.networkcusp.senderReceivers;

/**
 * A network address that can be use for whatever
 */
public final class ProtocolsNetworkAddress {
    private final String place;
    private final int port;

    public ProtocolsNetworkAddress(String place, int port) {
        this.place = place;
        this.port = port;
    }

    public int takePort() {
        return port;
    }

    public String obtainPlace() {
        return place;
    }

    @Override
    public String toString() {
        return place + ":" + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProtocolsNetworkAddress that = (ProtocolsNetworkAddress) o;

        if (port != that.port) return false;
        return place != null ? place.equals(that.place) : that.place == null;

    }

    @Override
    public int hashCode() {
        int result = place != null ? place.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

}
