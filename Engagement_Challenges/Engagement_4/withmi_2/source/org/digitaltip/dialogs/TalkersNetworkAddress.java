package org.digitaltip.dialogs;

/**
 * A network address that can be use for whatever
 */
public final class TalkersNetworkAddress {
    private final String main;
    private final int port;

    public TalkersNetworkAddress(String main, int port) {
        this.main = main;
        this.port = port;
    }

    public int fetchPort() {
        return port;
    }

    public String grabMain() {
        return main;
    }

    @Override
    public String toString() {
        return main + ":" + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TalkersNetworkAddress that = (TalkersNetworkAddress) o;

        if (port != that.port) return false;
        return main != null ? main.equals(that.main) : that.main == null;

    }

    @Override
    public int hashCode() {
        int result = main != null ? main.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

}
