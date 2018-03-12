package com.digitalpoint.dialogs;

/**
 * A network address that can be use for whatever
 */
public final class SenderReceiversNetworkAddress {
    private final String place;
    private final int port;

    public SenderReceiversNetworkAddress(String place, int port) {
        this.place = place;
        this.port = port;
    }

    public int pullPort() {
        return port;
    }

    public String grabPlace() {
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

        SenderReceiversNetworkAddress that = (SenderReceiversNetworkAddress) o;

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
