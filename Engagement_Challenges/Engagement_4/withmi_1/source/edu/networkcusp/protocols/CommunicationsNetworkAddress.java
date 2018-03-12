package edu.networkcusp.protocols;

/**
 * A network address that can be use for whatever
 */
public final class CommunicationsNetworkAddress {
    private final String host;
    private final int port;

    public CommunicationsNetworkAddress(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public int pullPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommunicationsNetworkAddress that = (CommunicationsNetworkAddress) o;

        if (port != that.port) return false;
        return host != null ? host.equals(that.host) : that.host == null;

    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

}
