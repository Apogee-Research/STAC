package org.digitaltip.chatroom;

import org.digitaltip.dialogs.TalkersConnection;
import org.digitaltip.dialogs.TalkersDeviation;
import org.digitaltip.dialogs.TalkersNetworkAddress;
import org.digitaltip.dialogs.TalkersPublicIdentity;

public class User implements Comparable<User> {
    private final String name;
    private final TalkersPublicIdentity identity;
    private TalkersConnection connection;

    private User(String name, TalkersPublicIdentity identity, TalkersConnection connection) {
        this.name = name;
        this.identity = identity;
        this.connection = connection;
    }

    public User(String name, TalkersConnection connection) {
        this(name, connection.takeTheirIdentity(), connection);
    }

    public User(String name, TalkersPublicIdentity identity) {
        this(name, identity, null);
    }

    public String takeName() {
        return name;
    }

    public TalkersPublicIdentity fetchIdentity() {
        return identity;
    }

    public TalkersConnection grabConnection() {
        return connection;
    }

    public void removeConnection() {
        connection = null;
    }

    public void fixConnection(TalkersConnection connection) throws TalkersDeviation {
        if (this.connection != null) {
            setConnectionEngine();
        }
        if (!connection.takeTheirIdentity().equals(identity)) {
            assignConnectionExecutor();
        }

        this.connection = connection;
    }

    private void assignConnectionExecutor() throws TalkersDeviation {
        throw new TalkersDeviation("Connection and public identity do not match");
    }

    private void setConnectionEngine() throws TalkersDeviation {
        throw new TalkersDeviation("User cannot have more than one connection");
    }

    public boolean hasConnection() {
        return connection != null;
    }

    public boolean hasCallbackAddress() {
        return identity.hasCallbackAddress();
    }

    public TalkersNetworkAddress obtainCallbackAddress() {
        return identity.grabCallbackAddress();
    }

    @Override
    public String toString() {
        return name + "\t" + identity.grabCallbackAddress();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User that = (User) o;

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
    public int compareTo(User otherCustomer) {
        // We want a way to consistently sort users, but we don't really care
        return takeName().compareTo(otherCustomer.takeName());
    }
}
