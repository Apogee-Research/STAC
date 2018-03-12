package net.computerpoint.chatroom;

import net.computerpoint.dialogs.ProtocolsConnection;
import net.computerpoint.dialogs.ProtocolsDeviation;
import net.computerpoint.dialogs.ProtocolsPublicIdentity;
import net.computerpoint.chatroom.store.WithMiConnectionsService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonConductor {
    private final HangIn withMi;

    /** maps user name to the WithMiUser */
    private final Map<String, Participant> nameToPerson = new HashMap<>();

    /** maps the identity to a WithMiUser */
    private final Map<ProtocolsPublicIdentity, Participant> idToPerson = new HashMap<>();

    /** a list of users we are no longer connected to */
    private final List<Participant> pastPersons = new ArrayList<>();

    /** Handles previous connections */
    private final WithMiConnectionsService connectionsService;

    public PersonConductor(HangIn withMi, WithMiConnectionsService connectionsService) throws ProtocolsDeviation {
        this.withMi = withMi;
        this.connectionsService = connectionsService;
        nameToPerson.putAll(connectionsService.readInPreviousConnections());
        pastPersons.addAll(nameToPerson.values());
        idToPerson.putAll(formMapFromIdentitiesToPersons(pastPersons));
    }

    /**
     * Creates a WithMiUser for the given connection, unless the identity
     * associated with the connection already belongs to a user.
     * If the identity already has a user that user is returned.
     * @return
     */
    private Participant getOrFormPerson(ProtocolsPublicIdentity identity, ProtocolsConnection connection) throws ProtocolsDeviation {

        if (idToPerson.containsKey(identity) ) {
            Participant oldPerson = idToPerson.get(identity);
            oldPerson.assignConnection(connection);

            // remove user from past users if they are there
            if (pastPersons.contains(oldPerson)) {
                pastPersons.remove(oldPerson);
            }

            return oldPerson;
        }

        String name = identity.fetchId();
        if (nameToPerson.containsKey(name)) {
            // already have a user with this name, we need to give them some different identifier...
            int suffixId = 1;
            String newName = name + "_" + Integer.toHexString(suffixId);
            while (nameToPerson.containsKey(newName)) {
                suffixId++;
                newName = name + "_" + Integer.toHexString(suffixId);
            }
            name = newName;
        }
        Participant person;
        if (connection != null) {
            person = new Participant(name, connection);
        } else {
            person = new Participant(name, identity);
        }

        return person;
    }

    public Participant fetchOrFormPerson(ProtocolsPublicIdentity identity) throws ProtocolsDeviation {
        return getOrFormPerson(identity, null);
    }

    public Participant fetchOrFormPerson(ProtocolsConnection connection) throws ProtocolsDeviation {
        return getOrFormPerson(connection.obtainTheirIdentity(), connection);
    }

    public boolean removeConnection(ProtocolsConnection connection) throws ProtocolsDeviation {
        ProtocolsPublicIdentity identity = connection.obtainTheirIdentity();
        Participant person = idToPerson.get(identity);
        if (person.takeConnection() == null) {
            // Already removed; may happen during shutdown
            return false;
        }
        if (person.takeConnection().equals(connection)) {
            return removeConnectionEngine(person);
        }
        throw new ProtocolsDeviation("Connection is not associated with correct user");
    }

    private boolean removeConnectionEngine(Participant person) {
        person.removeConnection();
        pastPersons.add(person);
        return true;
    }

    /**
     * @param name of user
     * @return true if the manager knows this name
     */
    public boolean knowsPerson(String name) {
        return nameToPerson.containsKey(name);
    }

    /**
     * Tells the connections service to store the user
     * @param person to store
     * @throws IOException
     */
    public void storePerson(Participant person) throws ProtocolsDeviation {
        String name = person.getName();
        ProtocolsPublicIdentity identity = person.fetchIdentity();
        nameToPerson.put(name, person);
        idToPerson.put(identity, person);
        ArrayList<Participant> storedPersons = new ArrayList<>(connectionsService.addPersonToFile(person));

        // check that the storedUsers matches known users
        if (!storedPersons.containsAll(nameToPerson.values())) {
            storePersonWorker();
        }
    }

    private void storePersonWorker() throws ProtocolsDeviation {
        throw new ProtocolsDeviation("Stored users and known users are not the same");
    }

    /**
     * Adds the user to known users and creates the message we print ourselves once we've connected to this user.
     * The message changes depending on whether or not we have previously connected to the user.
     * @param person
     * @param shouldBeKnownPerson
     * @param connection
     * @throws ProtocolsDeviation
     */
    public void addPersonToPersonHistory(Participant person, boolean shouldBeKnownPerson, ProtocolsConnection connection) throws ProtocolsDeviation {

        boolean previouslyConnected = knowsPerson(person.getName());
        StringBuilder stringBuilder = new StringBuilder();

        // if we should know the user and we don't, add that to the message
        // this will likely only happen when using the reconnect command
        if (shouldBeKnownPerson && !previouslyConnected) {
            stringBuilder.append("WARNING: " + person.getName() + " has a different identity. This may not be the same user");
        }

        if (!previouslyConnected) {
            
            stringBuilder.append("Connected to new user ");
        } else {
            stringBuilder.append("Reconnected to ");
        }

        stringBuilder.append(person.getName());
        if (person.hasCallbackAddress()) {
            stringBuilder.append(". callback on: " + person.fetchCallbackAddress());
        }
        withMi.deliverReceipt(true, 0, person);

        
        if (!previouslyConnected) {
            storePerson(person);
        }
        

        withMi.printPersonMsg(stringBuilder.toString());

    }

    public Participant takePerson(String name) {
        if (nameToPerson.containsKey(name)) {
            return nameToPerson.get(name);
        }
        return null;
    }

    public Participant grabPerson(ProtocolsPublicIdentity identity) {
        if (idToPerson.containsKey(identity)) {
            return idToPerson.get(identity);
        }
        return null;
    }

    public ProtocolsConnection getIdentityConnection(ProtocolsPublicIdentity identity) {
        Participant person = idToPerson.get(identity);
        if (person.hasConnection()) {
            return person.takeConnection();
        }
        return null;
    }

    public boolean hasIdentityConnection(ProtocolsPublicIdentity identity) {
        if (!idToPerson.containsKey(identity)) {
            return false;
        }
        Participant person = idToPerson.get(identity);
        return person.hasConnection();
    }

    public List<Participant> grabAllPersons() {
        return new ArrayList<>(idToPerson.values());
    }

    public List<Participant> fetchPastPersons() {
        return new ArrayList<>(pastPersons);
    }

    public List<Participant> obtainCurrentPersons() {
        List<Participant> persons = new ArrayList<>(nameToPerson.values());
        persons.removeAll(pastPersons);
        return persons;
    }

    /**
     * Creates a map from CommsPublicIdentities to WithMiUsers.
     * @param persons to be mapped
     * @return map
     */
    private Map<ProtocolsPublicIdentity, Participant> formMapFromIdentitiesToPersons(List<Participant> persons) {
        Map<ProtocolsPublicIdentity, Participant> identityToPersonMap = new HashMap<>();
        for (int b = 0; b < persons.size(); b++) {
            formMapFromIdentitiesToPersonsFunction(persons, identityToPersonMap, b);
        }
        return identityToPersonMap;
    }

    private void formMapFromIdentitiesToPersonsFunction(List<Participant> persons, Map<ProtocolsPublicIdentity, Participant> identityToPersonMap, int b) {
        new PersonConductorHelper(persons, identityToPersonMap, b).invoke();
    }

    private class PersonConductorHelper {
        private List<Participant> persons;
        private Map<ProtocolsPublicIdentity, Participant> identityToPersonMap;
        private int c;

        public PersonConductorHelper(List<Participant> persons, Map<ProtocolsPublicIdentity, Participant> identityToPersonMap, int c) {
            this.persons = persons;
            this.identityToPersonMap = identityToPersonMap;
            this.c = c;
        }

        public void invoke() {
            Participant person = persons.get(c);
            identityToPersonMap.put(person.fetchIdentity(), person);
        }
    }
}