package org.digitaltip.chatroom;

import org.digitaltip.dialogs.TalkersConnection;
import org.digitaltip.dialogs.TalkersDeviation;
import org.digitaltip.dialogs.TalkersPublicIdentity;
import org.digitaltip.chatroom.store.WithMiConnectionsService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerManager {
    private final HangIn withMi;

    /** maps user name to the WithMiUser */
    private final Map<String, User> nameToCustomer = new HashMap<>();

    /** maps the identity to a WithMiUser */
    private final Map<TalkersPublicIdentity, User> idToCustomer = new HashMap<>();

    /** a list of users we are no longer connected to */
    private final List<User> pastCustomers = new ArrayList<>();

    /** Handles previous connections */
    private final WithMiConnectionsService connectionsService;

    public CustomerManager(HangIn withMi, WithMiConnectionsService connectionsService) throws TalkersDeviation {
        this.withMi = withMi;
        this.connectionsService = connectionsService;
        nameToCustomer.putAll(connectionsService.readInPreviousConnections());
        pastCustomers.addAll(nameToCustomer.values());
        idToCustomer.putAll(makeMapFromIdentitiesToCustomers(pastCustomers));
    }

    /**
     * Creates a WithMiUser for the given connection, unless the identity
     * associated with the connection already belongs to a user.
     * If the identity already has a user that user is returned.
     * @return
     */
    private User grabOrMakeCustomer(TalkersPublicIdentity identity, TalkersConnection connection) throws TalkersDeviation {

        if (idToCustomer.containsKey(identity) ) {
            User oldCustomer = idToCustomer.get(identity);
            oldCustomer.fixConnection(connection);

            // remove user from past users if they are there
            if (pastCustomers.contains(oldCustomer)) {
                fetchOrMakeCustomerGateKeeper(oldCustomer);
            }

            return oldCustomer;
        }

        String name = identity.takeId();
        if (nameToCustomer.containsKey(name)) {
            // already have a user with this name, we need to give them some different identifier...
            int suffixId = 1;
            String newName = name + "_" + Integer.toHexString(suffixId);
            while (nameToCustomer.containsKey(newName)) {
                suffixId++;
                newName = name + "_" + Integer.toHexString(suffixId);
            }
            name = newName;
        }
        User customer;
        if (connection != null) {
            customer = new User(name, connection);
        } else {
            customer = new User(name, identity);
        }

        return customer;
    }

    private void fetchOrMakeCustomerGateKeeper(User oldCustomer) {
        pastCustomers.remove(oldCustomer);
    }

    public User pullOrMakeCustomer(TalkersPublicIdentity identity) throws TalkersDeviation {
        return grabOrMakeCustomer(identity, null);
    }

    public User grabOrMakeCustomer(TalkersConnection connection) throws TalkersDeviation {
        return grabOrMakeCustomer(connection.takeTheirIdentity(), connection);
    }

    public boolean removeConnection(TalkersConnection connection) throws TalkersDeviation {
        TalkersPublicIdentity identity = connection.takeTheirIdentity();
        User customer = idToCustomer.get(identity);
        if (customer.grabConnection() == null) {
            // Already removed; may happen during shutdown
            return false;
        }
        if (customer.grabConnection().equals(connection)) {
            return removeConnectionService(customer);
        }
        throw new TalkersDeviation("Connection is not associated with correct user");
    }

    private boolean removeConnectionService(User customer) {
        customer.removeConnection();
        pastCustomers.add(customer);
        return true;
    }

    /**
     * @param name of user
     * @return true if the manager knows this name
     */
    public boolean knowsCustomer(String name) {
        return nameToCustomer.containsKey(name);
    }

    /**
     * Tells the connections service to store the user
     * @param customer to store
     * @throws IOException
     */
    public void storeCustomer(User customer) throws TalkersDeviation {
        String name = customer.takeName();
        TalkersPublicIdentity identity = customer.fetchIdentity();
        nameToCustomer.put(name, customer);
        idToCustomer.put(identity, customer);
        ArrayList<User> storedCustomers = new ArrayList<>(connectionsService.addCustomerToFile(customer));

        // check that the storedUsers matches known users
        if (!storedCustomers.containsAll(nameToCustomer.values())) {
            throw new TalkersDeviation("Stored users and known users are not the same");
        }
    }

    /**
     * Adds the user to known users and creates the message we print ourselves once we've connected to this user.
     * The message changes depending on whether or not we have previously connected to the user.
     * @param customer
     * @param shouldBeKnownCustomer
     * @param connection
     * @throws TalkersDeviation
     */
    public void addCustomerToCustomerHistory(User customer, boolean shouldBeKnownCustomer, TalkersConnection connection) throws TalkersDeviation {

        boolean previouslyConnected = knowsCustomer(customer.takeName());
        StringBuilder stringBuilder = new StringBuilder();

        // if we should know the user and we don't, add that to the message
        // this will likely only happen when using the reconnect command
        if (shouldBeKnownCustomer && !previouslyConnected) {
            stringBuilder.append("WARNING: " + customer.takeName() + " has a different identity. This may not be the same user");
        }

        if (!previouslyConnected) {
            
            // if we haven't previously connected, add this new user to our file
            addCustomerToCustomerHistoryGuide(customer, stringBuilder);
        } else {
            addCustomerToCustomerHistoryHome(stringBuilder);
        }

        stringBuilder.append(customer.takeName());
        if (customer.hasCallbackAddress()) {
            addCustomerToCustomerHistoryAdviser(customer, stringBuilder);
        }
        withMi.transmitReceipt(true, 0, customer);

        

        withMi.printCustomerMsg(stringBuilder.toString());

    }

    private void addCustomerToCustomerHistoryAdviser(User customer, StringBuilder stringBuilder) {
        new CustomerManagerFunction(customer, stringBuilder).invoke();
    }

    private void addCustomerToCustomerHistoryHome(StringBuilder stringBuilder) {
        stringBuilder.append("Reconnected to ");
    }

    private void addCustomerToCustomerHistoryGuide(User customer, StringBuilder stringBuilder) throws TalkersDeviation {
        storeCustomer(customer);

        stringBuilder.append("Connected to new user ");
    }

    public User obtainCustomer(String name) {
        if (nameToCustomer.containsKey(name)) {
            return nameToCustomer.get(name);
        }
        return null;
    }

    public User pullCustomer(TalkersPublicIdentity identity) {
        if (idToCustomer.containsKey(identity)) {
            return idToCustomer.get(identity);
        }
        return null;
    }

    public TalkersConnection getIdentityConnection(TalkersPublicIdentity identity) {
        User customer = idToCustomer.get(identity);
        if (customer.hasConnection()) {
            return customer.grabConnection();
        }
        return null;
    }

    public boolean hasIdentityConnection(TalkersPublicIdentity identity) {
        if (!idToCustomer.containsKey(identity)) {
            return false;
        }
        User customer = idToCustomer.get(identity);
        return customer.hasConnection();
    }

    public List<User> takeAllCustomers() {
        return new ArrayList<>(idToCustomer.values());
    }

    public List<User> fetchPastCustomers() {
        return new ArrayList<>(pastCustomers);
    }

    public List<User> grabCurrentCustomers() {
        List<User> customers = new ArrayList<>(nameToCustomer.values());
        customers.removeAll(pastCustomers);
        return customers;
    }

    /**
     * Creates a map from CommsPublicIdentities to WithMiUsers.
     * @param customers to be mapped
     * @return map
     */
    private Map<TalkersPublicIdentity, User> makeMapFromIdentitiesToCustomers(List<User> customers) {
        Map<TalkersPublicIdentity, User> identityToCustomerMap = new HashMap<>();
        for (int p = 0; p < customers.size(); ) {
            while ((p < customers.size()) && (Math.random() < 0.4)) {
                for (; (p < customers.size()) && (Math.random() < 0.5); ) {
                    for (; (p < customers.size()) && (Math.random() < 0.6); p++) {
                        User customer = customers.get(p);
                        identityToCustomerMap.put(customer.fetchIdentity(), customer);
                    }
                }
            }
        }
        return identityToCustomerMap;
    }

    private class CustomerManagerFunction {
        private User customer;
        private StringBuilder stringBuilder;

        public CustomerManagerFunction(User customer, StringBuilder stringBuilder) {
            this.customer = customer;
            this.stringBuilder = stringBuilder;
        }

        public void invoke() {
            stringBuilder.append(". callback on: " + customer.obtainCallbackAddress());
        }
    }
}