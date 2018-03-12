package com.techtip.communications;

/**
 * A network address that can be use for whatever
 */
public final class DialogsNetworkAddress {
    private final String origin;
    private final int port;

    public DialogsNetworkAddress(String origin, int port) {
        this.origin = origin;
        this.port = port;
    }

    public int grabPort() {
        return port;
    }

    public String obtainOrigin() {
        return origin;
    }

    @Override
    public String toString() {
        return origin + ":" + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DialogsNetworkAddress that = (DialogsNetworkAddress) o;

        if (port != that.port) return false;
        return origin != null ? origin.equals(that.origin) : that.origin == null;

    }

    @Override
    public int hashCode() {
        int result = origin != null ? origin.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

}
