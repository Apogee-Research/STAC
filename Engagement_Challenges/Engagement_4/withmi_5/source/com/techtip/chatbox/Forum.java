package com.techtip.chatbox;

import com.techtip.communications.DialogsNetworkAddress;
import com.techtip.communications.DialogsPublicIdentity;
import com.techtip.communications.DialogsPublicIdentityBuilder;
import com.techtip.numerical.CipherPublicKey;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A chat room
 */
public class Forum {
    // max number of characters in a chat name
    public final static int MAX_NAME_LENGTH = 19;

    // null user
    public final static DialogsPublicIdentity NULL_IDENTITY = new DialogsPublicIdentityBuilder().fixId("null").assignPublicKey(new CipherPublicKey(new BigInteger("65537"), new BigInteger("11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111"))).setCallbackAddress(new DialogsNetworkAddress("localhost", -1000)).formDialogsPublicIdentity();

    // max number of users not including us
    public final static int MAX_NUM_OF_USERS = 4;


    /**
     * the uniqueId used in every message sent to this chat to signal
     * that it came from this chat every user in this chat knows and
     * stores this id
     */
    private final String uniqueId;

    /**
     * the name the user associates with this chat
     */
    private final String name;

    /**
     * the users in this chat
     */
    private final Set<WithMiUser> customers = new HashSet<>();

    private final List<String> unreadMessages = new ArrayList<>();

    private final DropBy withMi;

    public Forum(DropBy withMi, String name, String uniqueId) {
        this.withMi = withMi;
        this.name = name;
        this.uniqueId = uniqueId;
    }

    /**
     * @return name of chat
     */
    public String obtainName() {
        return name;
    }

    /**
     * Returns a list of unread messages, then deletes the messages
     *
     * @return list of unread messages
     */
    public List<String> readUnreadMessages() {
        List<String> temp = new ArrayList<>(unreadMessages);
        unreadMessages.clear();
        return temp;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void addCustomer(WithMiUser customer) {
        if (canAddMoreCustomers()) {
            customers.add(customer);
        } else {
            addCustomerWorker(customer);
        }
    }

    private void addCustomerWorker(WithMiUser customer) {
        withMi.printCustomerMsg("Not adding user " + customer.pullName() + " because " + name +
                " has reached the maximum number of users");
    }

    public List<WithMiUser> fetchCustomers() {
        List<WithMiUser> sortedCustomers = new ArrayList<>(customers);
        Collections.sort(sortedCustomers);
        return sortedCustomers;
    }

    public boolean containsCustomer(WithMiUser customer) {
        return customers.contains(customer);
    }

    public void removeCustomer(WithMiUser customer) {
        customers.remove(customer);
    }

    public boolean canAddMoreCustomers() {
        return customers.size() < MAX_NUM_OF_USERS;
    }

    public void storeUnreadMessage(String message) {
        unreadMessages.add(message);
    }
}
