package edu.networkcusp.chatbox;

import edu.networkcusp.protocols.CommunicationsConnection;
import edu.networkcusp.protocols.CommunicationsFailure;
import edu.networkcusp.protocols.CommunicationsNetworkAddress;
import edu.networkcusp.protocols.CommunicationsPublicIdentity;

public class WithMiUser implements Comparable<WithMiUser> {
    private final String name;
    private final CommunicationsPublicIdentity identity;
    private CommunicationsConnection connection;

    private WithMiUser(String name, CommunicationsPublicIdentity identity, CommunicationsConnection connection) {
        this.name = name;
        this.identity = identity;
        this.connection = connection;
    }

    public WithMiUser(String name, CommunicationsConnection connection) {
        this(name, connection.fetchTheirIdentity(), connection);
    }

    public WithMiUser(String name, CommunicationsPublicIdentity identity) {
        this(name, identity, null);
    }

    public String obtainName() {
        return name;
    }

    public CommunicationsPublicIdentity getIdentity() {
        return identity;
    }

    public CommunicationsConnection obtainConnection() {
        return connection;
    }

    public void removeConnection() {
        connection = null;
    }

    public void setConnection(CommunicationsConnection connection) throws CommunicationsFailure {
        if (this.connection != null) {
            fixConnectionEntity();
        }
        if (!connection.fetchTheirIdentity().equals(identity)) {
            throw new CommunicationsFailure("Connection and public identity do not match");
        }

        this.connection = connection;
    }

    private void fixConnectionEntity() throws CommunicationsFailure {
        throw new CommunicationsFailure("User cannot have more than one connection");
    }

    public boolean hasConnection() {
        return connection != null;
    }

    public boolean hasCallbackAddress() {
        return identity.hasCallbackAddress();
    }

    public CommunicationsNetworkAddress takeCallbackAddress() {
        return identity.obtainCallbackAddress();
    }

    @Override
    public String toString() {
        return name + "\t" + identity.obtainCallbackAddress();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WithMiUser that = (WithMiUser) o;

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
    public int compareTo(WithMiUser otherMember) {
        // We want a way to consistently sort users, but we don't really care
        return obtainName().compareTo(otherMember.obtainName());
    }
}
