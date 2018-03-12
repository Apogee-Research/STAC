package net.robotictip.dropbys;

import net.robotictip.protocols.SenderReceiversConnection;
import net.robotictip.protocols.SenderReceiversTrouble;
import net.robotictip.protocols.SenderReceiversPublicIdentity;
import net.robotictip.dropbys.persist.WithMiConnectionsService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserManager {
    private final HangIn withMi;

    /** maps user name to the WithMiUser */
    private final Map<String, Chatee> nameToUser = new HashMap<>();

    /** maps the identity to a WithMiUser */
    private final Map<SenderReceiversPublicIdentity, Chatee> idToUser = new HashMap<>();

    /** a list of users we are no longer connected to */
    private final List<Chatee> pastUsers = new ArrayList<>();

    /** Handles previous connections */
    private final WithMiConnectionsService connectionsService;

    public UserManager(HangIn withMi, WithMiConnectionsService connectionsService) throws SenderReceiversTrouble {
        this.withMi = withMi;
        this.connectionsService = connectionsService;
        nameToUser.putAll(connectionsService.readInPreviousConnections());
        pastUsers.addAll(nameToUser.values());
        idToUser.putAll(generateMapFromIdentitiesToUsers(pastUsers));
    }

    /**
     * Creates a WithMiUser for the given connection, unless the identity
     * associated with the connection already belongs to a user.
     * If the identity already has a user that user is returned.
     * @return
     */
    private Chatee getOrGenerateUser(SenderReceiversPublicIdentity identity, SenderReceiversConnection connection) throws SenderReceiversTrouble {

        if (idToUser.containsKey(identity) ) {
            Chatee oldUser = idToUser.get(identity);
            oldUser.defineConnection(connection);

            // remove user from past users if they are there
            if (pastUsers.contains(oldUser)) {
                pastUsers.remove(oldUser);
            }

            return oldUser;
        }

        String name = identity.getId();
        if (nameToUser.containsKey(name)) {
            // already have a user with this name, we need to give them some different identifier...
            int suffixId = 1;
            String newName = name + "_" + Integer.toHexString(suffixId);
            while (nameToUser.containsKey(newName)) {
                suffixId++;
                newName = name + "_" + Integer.toHexString(suffixId);
            }
            name = newName;
        }
        Chatee user;
        if (connection != null) {
            user = new Chatee(name, connection);
        } else {
            user = new Chatee(name, identity);
        }

        return user;
    }

    public Chatee fetchOrGenerateUser(SenderReceiversPublicIdentity identity) throws SenderReceiversTrouble {
        return getOrGenerateUser(identity, null);
    }

    public Chatee pullOrGenerateUser(SenderReceiversConnection connection) throws SenderReceiversTrouble {
        return getOrGenerateUser(connection.grabTheirIdentity(), connection);
    }

    public boolean removeConnection(SenderReceiversConnection connection) throws SenderReceiversTrouble {
        SenderReceiversPublicIdentity identity = connection.grabTheirIdentity();
        Chatee user = idToUser.get(identity);
        if (user.takeConnection() == null) {
            // Already removed; may happen during shutdown
            return false;
        }
        if (user.takeConnection().equals(connection)) {
            user.removeConnection();
            pastUsers.add(user);
            return true;
        }
        throw new SenderReceiversTrouble("Connection is not associated with correct user");
    }

    /**
     * @param name of user
     * @return true if the manager knows this name
     */
    public boolean knowsUser(String name) {
        return nameToUser.containsKey(name);
    }

    /**
     * Tells the connections service to store the user
     * @param user to store
     * @throws IOException
     */
    public void storeUser(Chatee user) throws SenderReceiversTrouble {
        String name = user.obtainName();
        SenderReceiversPublicIdentity identity = user.pullIdentity();
        nameToUser.put(name, user);
        idToUser.put(identity, user);
        ArrayList<Chatee> storedUsers = new ArrayList<>(connectionsService.addUserToFile(user));

        // check that the storedUsers matches known users
        if (!storedUsers.containsAll(nameToUser.values())) {
            new UserManagerUtility().invoke();
        }
    }

    /**
     * Adds the user to known users and creates the message we print ourselves once we've connected to this user.
     * The message changes depending on whether or not we have previously connected to the user.
     * @param user
     * @param shouldBeKnownUser
     * @param connection
     * @throws SenderReceiversTrouble
     */
    public void addUserToUserHistory(Chatee user, boolean shouldBeKnownUser, SenderReceiversConnection connection) throws SenderReceiversTrouble {

        boolean previouslyConnected = knowsUser(user.obtainName());
        StringBuilder stringBuilder = new StringBuilder();

        // if we should know the user and we don't, add that to the message
        // this will likely only happen when using the reconnect command
        if (shouldBeKnownUser && !previouslyConnected) {
            stringBuilder.append("WARNING: " + user.obtainName() + " has a different identity. This may not be the same user");
        }

        if (!previouslyConnected) {
            
            // if we haven't previously connected, add this new user to our file
            addUserToUserHistoryTarget(user, stringBuilder);
        } else {
            addUserToUserHistoryFunction(stringBuilder);
        }

        stringBuilder.append(user.obtainName());
        if (user.hasCallbackAddress()) {
            stringBuilder.append(". callback on: " + user.fetchCallbackAddress());
        }
        withMi.transferReceipt(true, 0, user);

        

        withMi.printUserMsg(stringBuilder.toString());

    }

    private void addUserToUserHistoryFunction(StringBuilder stringBuilder) {
        stringBuilder.append("Reconnected to ");
    }

    private void addUserToUserHistoryTarget(Chatee user, StringBuilder stringBuilder) throws SenderReceiversTrouble {
        storeUser(user);

        stringBuilder.append("Connected to new user ");
    }

    public Chatee getUser(String name) {
        if (nameToUser.containsKey(name)) {
            return nameToUser.get(name);
        }
        return null;
    }

    public Chatee obtainUser(SenderReceiversPublicIdentity identity) {
        if (idToUser.containsKey(identity)) {
            return idToUser.get(identity);
        }
        return null;
    }

    public SenderReceiversConnection pullIdentityConnection(SenderReceiversPublicIdentity identity) {
        Chatee user = idToUser.get(identity);
        if (user.hasConnection()) {
            return user.takeConnection();
        }
        return null;
    }

    public boolean hasIdentityConnection(SenderReceiversPublicIdentity identity) {
        if (!idToUser.containsKey(identity)) {
            return false;
        }
        Chatee user = idToUser.get(identity);
        return user.hasConnection();
    }

    public List<Chatee> obtainAllUsers() {
        return new ArrayList<>(idToUser.values());
    }

    public List<Chatee> pullPastUsers() {
        return new ArrayList<>(pastUsers);
    }

    public List<Chatee> fetchCurrentUsers() {
        List<Chatee> users = new ArrayList<>(nameToUser.values());
        users.removeAll(pastUsers);
        return users;
    }

    /**
     * Creates a map from CommsPublicIdentities to WithMiUsers.
     * @param users to be mapped
     * @return map
     */
    private Map<SenderReceiversPublicIdentity, Chatee> generateMapFromIdentitiesToUsers(List<Chatee> users) {
        Map<SenderReceiversPublicIdentity, Chatee> identityToUserMap = new HashMap<>();
        for (int a = 0; a < users.size(); a++) {
            Chatee user = users.get(a);
            identityToUserMap.put(user.pullIdentity(), user);
        }
        return identityToUserMap;
    }

    private class UserManagerUtility {
        public void invoke() throws SenderReceiversTrouble {
            throw new SenderReceiversTrouble("Stored users and known users are not the same");
        }
    }
}