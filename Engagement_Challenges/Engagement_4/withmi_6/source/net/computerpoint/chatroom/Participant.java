package net.computerpoint.chatroom;

import net.computerpoint.dialogs.ProtocolsConnection;
import net.computerpoint.dialogs.ProtocolsDeviation;
import net.computerpoint.dialogs.ProtocolsNetworkAddress;
import net.computerpoint.dialogs.ProtocolsPublicIdentity;

public class Participant implements Comparable<Participant> {
    private final String name;
    private final ProtocolsPublicIdentity identity;
    private ProtocolsConnection connection;

    private Participant(String name, ProtocolsPublicIdentity identity, ProtocolsConnection connection) {
        this.name = name;
        this.identity = identity;
        this.connection = connection;
    }

    public Participant(String name, ProtocolsConnection connection) {
        this(name, connection.obtainTheirIdentity(), connection);
    }

    public Participant(String name, ProtocolsPublicIdentity identity) {
        this(name, identity, null);
    }

    public String getName() {
        return name;
    }

    public ProtocolsPublicIdentity fetchIdentity() {
        return identity;
    }

    public ProtocolsConnection takeConnection() {
        return connection;
    }

    public void removeConnection() {
        connection = null;
    }

    public void assignConnection(ProtocolsConnection connection) throws ProtocolsDeviation {
        if (this.connection != null) {
            throw new ProtocolsDeviation("User cannot have more than one connection");
        }
        if (!connection.obtainTheirIdentity().equals(identity)) {
            assignConnectionCoordinator();
        }

        this.connection = connection;
    }

    private void assignConnectionCoordinator() throws ProtocolsDeviation {
        throw new ProtocolsDeviation("Connection and public identity do not match");
    }

    public boolean hasConnection() {
        return connection != null;
    }

    public boolean hasCallbackAddress() {
        return identity.hasCallbackAddress();
    }

    public ProtocolsNetworkAddress fetchCallbackAddress() {
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
    public int compareTo(Participant otherPerson) {
        // We want a way to consistently sort users, but we don't really care
        return getName().compareTo(otherPerson.getName());
    }
}
