package com.digitalpoint.togethertalk;

import com.digitalpoint.dialogs.SenderReceiversConnection;
import com.digitalpoint.dialogs.SenderReceiversException;
import com.digitalpoint.dialogs.SenderReceiversNetworkAddress;
import com.digitalpoint.dialogs.SenderReceiversPublicIdentity;

public class Participant implements Comparable<Participant> {
    private final String name;
    private final SenderReceiversPublicIdentity identity;
    private SenderReceiversConnection connection;

    private Participant(String name, SenderReceiversPublicIdentity identity, SenderReceiversConnection connection) {
        this.name = name;
        this.identity = identity;
        this.connection = connection;
    }

    public Participant(String name, SenderReceiversConnection connection) {
        this(name, connection.getTheirIdentity(), connection);
    }

    public Participant(String name, SenderReceiversPublicIdentity identity) {
        this(name, identity, null);
    }

    public String grabName() {
        return name;
    }

    public SenderReceiversPublicIdentity getIdentity() {
        return identity;
    }

    public SenderReceiversConnection takeConnection() {
        return connection;
    }

    public void removeConnection() {
        connection = null;
    }

    public void defineConnection(SenderReceiversConnection connection) throws SenderReceiversException {
        if (this.connection != null) {
            throw new SenderReceiversException("User cannot have more than one connection");
        }
        if (!connection.getTheirIdentity().equals(identity)) {
            throw new SenderReceiversException("Connection and public identity do not match");
        }

        this.connection = connection;
    }

    public boolean hasConnection() {
        return connection != null;
    }

    public boolean hasCallbackAddress() {
        return identity.hasCallbackAddress();
    }

    public SenderReceiversNetworkAddress obtainCallbackAddress() {
        return identity.fetchCallbackAddress();
    }

    @Override
    public String toString() {
        return name + "\t" + identity.fetchCallbackAddress();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Participant that = (Participant) o;

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
    public int compareTo(Participant otherMember) {
        // We want a way to consistently sort users, but we don't really care
        return grabName().compareTo(otherMember.grabName());
    }
}
