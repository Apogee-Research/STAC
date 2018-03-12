package com.techtip.chatbox;

import com.techtip.communications.DialogsConnection;
import com.techtip.communications.DialogsDeviation;
import com.techtip.communications.DialogsNetworkAddress;
import com.techtip.communications.DialogsPublicIdentity;

public class WithMiUser implements Comparable<WithMiUser> {
    private final String name;
    private final DialogsPublicIdentity identity;
    private DialogsConnection connection;

    private WithMiUser(String name, DialogsPublicIdentity identity, DialogsConnection connection) {
        this.name = name;
        this.identity = identity;
        this.connection = connection;
    }

    public WithMiUser(String name, DialogsConnection connection) {
        this(name, connection.fetchTheirIdentity(), connection);
    }

    public WithMiUser(String name, DialogsPublicIdentity identity) {
        this(name, identity, null);
    }

    public String pullName() {
        return name;
    }

    public DialogsPublicIdentity grabIdentity() {
        return identity;
    }

    public DialogsConnection getConnection() {
        return connection;
    }

    public void removeConnection() {
        connection = null;
    }

    public void defineConnection(DialogsConnection connection) throws DialogsDeviation {
        if (this.connection != null) {
            setConnectionHelper();
        }
        if (!connection.fetchTheirIdentity().equals(identity)) {
            defineConnectionSupervisor();
        }

        this.connection = connection;
    }

    private void defineConnectionSupervisor() throws DialogsDeviation {
        throw new DialogsDeviation("Connection and public identity do not match");
    }

    private void setConnectionHelper() throws DialogsDeviation {
        new WithMiCustomerGuide().invoke();
    }

    public boolean hasConnection() {
        return connection != null;
    }

    public boolean hasCallbackAddress() {
        return identity.hasCallbackAddress();
    }

    public DialogsNetworkAddress obtainCallbackAddress() {
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
    public int compareTo(WithMiUser otherCustomer) {
        // We want a way to consistently sort users, but we don't really care
        return pullName().compareTo(otherCustomer.pullName());
    }

    private class WithMiCustomerGuide {
        public void invoke() throws DialogsDeviation {
            throw new DialogsDeviation("User cannot have more than one connection");
        }
    }
}
