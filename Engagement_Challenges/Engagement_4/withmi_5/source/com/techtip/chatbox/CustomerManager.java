package com.techtip.chatbox;

import com.techtip.communications.DialogsConnection;
import com.techtip.communications.DialogsDeviation;
import com.techtip.communications.DialogsPublicIdentity;
import com.techtip.chatbox.keep.WithMiConnectionsService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerManager {
    private final DropBy withMi;

    /** maps user name to the WithMiUser */
    private final Map<String, WithMiUser> nameToCustomer = new HashMap<>();

    /** maps the identity to a WithMiUser */
    private final Map<DialogsPublicIdentity, WithMiUser> idToCustomer = new HashMap<>();

    /** a list of users we are no longer connected to */
    private final List<WithMiUser> pastCustomers = new ArrayList<>();

    /** Handles previous connections */
    private final WithMiConnectionsService connectionsService;

    public CustomerManager(DropBy withMi, WithMiConnectionsService connectionsService) throws DialogsDeviation {
        this.withMi = withMi;
        this.connectionsService = connectionsService;
        nameToCustomer.putAll(connectionsService.readInPreviousConnections());
        pastCustomers.addAll(nameToCustomer.values());
        idToCustomer.putAll(formMapFromIdentitiesToCustomers(pastCustomers));
    }

    /**
     * Creates a WithMiUser for the given connection, unless the identity
     * associated with the connection already belongs to a user.
     * If the identity already has a user that user is returned.
     * @return
     */
    private WithMiUser getOrFormCustomer(DialogsPublicIdentity identity, DialogsConnection connection) throws DialogsDeviation {

        if (idToCustomer.containsKey(identity) ) {
            WithMiUser oldCustomer = idToCustomer.get(identity);
            oldCustomer.defineConnection(connection);

            // remove user from past users if they are there
            if (pastCustomers.contains(oldCustomer)) {
                fetchOrFormCustomerHelp(oldCustomer);
            }

            return oldCustomer;
        }

        String name = identity.getId();
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
        WithMiUser customer;
        if (connection != null) {
            customer = new WithMiUser(name, connection);
        } else {
            customer = new WithMiUser(name, identity);
        }

        return customer;
    }

    private void fetchOrFormCustomerHelp(WithMiUser oldCustomer) {
        pastCustomers.remove(oldCustomer);
    }

    public WithMiUser pullOrFormCustomer(DialogsPublicIdentity identity) throws DialogsDeviation {
        return getOrFormCustomer(identity, null);
    }

    public WithMiUser grabOrFormCustomer(DialogsConnection connection) throws DialogsDeviation {
        return getOrFormCustomer(connection.fetchTheirIdentity(), connection);
    }

    public boolean removeConnection(DialogsConnection connection) throws DialogsDeviation {
        DialogsPublicIdentity identity = connection.fetchTheirIdentity();
        WithMiUser customer = idToCustomer.get(identity);
        if (customer.getConnection() == null) {
            // Already removed; may happen during shutdown
            return false;
        }
        if (customer.getConnection().equals(connection)) {
            return removeConnectionCoordinator(customer);
        }
        throw new DialogsDeviation("Connection is not associated with correct user");
    }

    private boolean removeConnectionCoordinator(WithMiUser customer) {
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
    public void storeCustomer(WithMiUser customer) throws DialogsDeviation {
        String name = customer.pullName();
        DialogsPublicIdentity identity = customer.grabIdentity();
        nameToCustomer.put(name, customer);
        idToCustomer.put(identity, customer);
        ArrayList<WithMiUser> storedCustomers = new ArrayList<>(connectionsService.addCustomerToFile(customer));

        // check that the storedUsers matches known users
        if (!storedCustomers.containsAll(nameToCustomer.values())) {
            throw new DialogsDeviation("Stored users and known users are not the same");
        }
    }

    /**
     * Adds the user to known users and creates the message we print ourselves once we've connected to this user.
     * The message changes depending on whether or not we have previously connected to the user.
     * @param customer
     * @param shouldBeKnownCustomer
     * @param connection
     * @throws DialogsDeviation
     */
    public void addCustomerToCustomerHistory(WithMiUser customer, boolean shouldBeKnownCustomer, DialogsConnection connection) throws DialogsDeviation {

        boolean previouslyConnected = knowsCustomer(customer.pullName());
        StringBuilder stringBuilder = new StringBuilder();

        // if we should know the user and we don't, add that to the message
        // this will likely only happen when using the reconnect command
        if (shouldBeKnownCustomer && !previouslyConnected) {
            addCustomerToCustomerHistoryGuide(customer, stringBuilder);
        }

        if (!previouslyConnected) {

            addCustomerToCustomerHistoryTarget(stringBuilder);
        } else {
            stringBuilder.append("Reconnected to ");
        }

        stringBuilder.append(customer.pullName());
        if (customer.hasCallbackAddress()) {
            stringBuilder.append(". callback on: " + customer.obtainCallbackAddress());
        }
        withMi.transmitReceipt(true, 0, customer);

        
        if (!previouslyConnected) {
            addCustomerToCustomerHistoryUtility(customer);
        }
        

        withMi.printCustomerMsg(stringBuilder.toString());

    }

    private void addCustomerToCustomerHistoryUtility(WithMiUser customer) throws DialogsDeviation {
        storeCustomer(customer);
    }

    private void addCustomerToCustomerHistoryTarget(StringBuilder stringBuilder) {
        stringBuilder.append("Connected to new user ");
    }

    private void addCustomerToCustomerHistoryGuide(WithMiUser customer, StringBuilder stringBuilder) {
        stringBuilder.append("WARNING: " + customer.pullName() + " has a different identity. This may not be the same user");
    }

    public WithMiUser obtainCustomer(String name) {
        if (nameToCustomer.containsKey(name)) {
            return nameToCustomer.get(name);
        }
        return null;
    }

    public WithMiUser takeCustomer(DialogsPublicIdentity identity) {
        if (idToCustomer.containsKey(identity)) {
            return idToCustomer.get(identity);
        }
        return null;
    }

    public DialogsConnection getIdentityConnection(DialogsPublicIdentity identity) {
        WithMiUser customer = idToCustomer.get(identity);
        if (customer.hasConnection()) {
            return customer.getConnection();
        }
        return null;
    }

    public boolean hasIdentityConnection(DialogsPublicIdentity identity) {
        if (!idToCustomer.containsKey(identity)) {
            return false;
        }
        WithMiUser customer = idToCustomer.get(identity);
        return customer.hasConnection();
    }

    public List<WithMiUser> takeAllCustomers() {
        return new ArrayList<>(idToCustomer.values());
    }

    public List<WithMiUser> obtainPastCustomers() {
        return new ArrayList<>(pastCustomers);
    }

    public List<WithMiUser> pullCurrentCustomers() {
        List<WithMiUser> customers = new ArrayList<>(nameToCustomer.values());
        customers.removeAll(pastCustomers);
        return customers;
    }

    /**
     * Creates a map from CommsPublicIdentities to WithMiUsers.
     * @param customers to be mapped
     * @return map
     */
    private Map<DialogsPublicIdentity, WithMiUser> formMapFromIdentitiesToCustomers(List<WithMiUser> customers) {
        Map<DialogsPublicIdentity, WithMiUser> identityToCustomerMap = new HashMap<>();
        for (int k = 0; k < customers.size(); k++) {
            formMapFromIdentitiesToCustomersGateKeeper(customers, identityToCustomerMap, k);
        }
        return identityToCustomerMap;
    }

    private void formMapFromIdentitiesToCustomersGateKeeper(List<WithMiUser> customers, Map<DialogsPublicIdentity, WithMiUser> identityToCustomerMap, int p) {
        WithMiUser customer = customers.get(p);
        identityToCustomerMap.put(customer.grabIdentity(), customer);
    }
}