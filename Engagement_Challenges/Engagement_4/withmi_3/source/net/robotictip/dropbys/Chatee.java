package net.robotictip.dropbys;

import net.robotictip.protocols.SenderReceiversConnection;
import net.robotictip.protocols.SenderReceiversTrouble;
import net.robotictip.protocols.SenderReceiversNetworkAddress;
import net.robotictip.protocols.SenderReceiversPublicIdentity;

public class Chatee implements Comparable<Chatee> {
    private final String name;
    private final SenderReceiversPublicIdentity identity;
    private SenderReceiversConnection connection;

    private Chatee(String name, SenderReceiversPublicIdentity identity, SenderReceiversConnection connection) {
        this.name = name;
        this.identity = identity;
        this.connection = connection;
    }

    public Chatee(String name, SenderReceiversConnection connection) {
        this(name, connection.grabTheirIdentity(), connection);
    }

    public Chatee(String name, SenderReceiversPublicIdentity identity) {
        this(name, identity, null);
    }

    public String obtainName() {
        return name;
    }

    public SenderReceiversPublicIdentity pullIdentity() {
        return identity;
    }

    public SenderReceiversConnection takeConnection() {
        return connection;
    }

    public void removeConnection() {
        connection = null;
    }

    public void defineConnection(SenderReceiversConnection connection) throws SenderReceiversTrouble {
        if (this.connection != null) {
            throw new SenderReceiversTrouble("User cannot have more than one connection");
        }
        if (!connection.grabTheirIdentity().equals(identity)) {
            throw new SenderReceiversTrouble("Connection and public identity do not match");
        }

        this.connection = connection;
    }

    public boolean hasConnection() {
        return connection != null;
    }

    public boolean hasCallbackAddress() {
        return identity.hasCallbackAddress();
    }

    public SenderReceiversNetworkAddress fetchCallbackAddress() {
        return identity.getCallbackAddress();
    }

    @Override
    public String toString() {
        return name + "\t" + identity.getCallbackAddress();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Chatee that = (Chatee) o;

        if (!name.equals(that.name)) return false;
        return identity.equals(that.identity);

    }

    @Override
    public int hashCode() {
        // remoteHost and remotePort aren't used in this calculation since
        // they may change and don't really matter for 'who' this user is.
        // Perhaps this is a mistake and we'll want to include them in the future

        int result = name.hashCode();
        result = 31 * result + identity.hashCode();
        return result;
    }


    @Override
    public int compareTo(Chatee otherUser) {
        // We want a way to consistently sort users, but we don't really care
        return obtainName().compareTo(otherUser.obtainName());
    }
}
