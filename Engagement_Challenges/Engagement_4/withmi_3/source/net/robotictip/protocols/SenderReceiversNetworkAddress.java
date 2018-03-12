package net.robotictip.protocols;

/**
 * A network address that can be use for whatever
 */
public final class SenderReceiversNetworkAddress {
    private final String home;
    private final int port;

    public SenderReceiversNetworkAddress(String home, int port) {
        this.home = home;
        this.port = port;
    }

    public int pullPort() {
        return port;
    }

    public String getHome() {
        return home;
    }

    @Override
    public String toString() {
        return home + ":" + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SenderReceiversNetworkAddress that = (SenderReceiversNetworkAddress) o;

        if (port != that.port) return false;
        return home != null ? home.equals(that.home) : that.home == null;

    }

    @Override
    public int hashCode() {
        int result = home != null ? home.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

}
