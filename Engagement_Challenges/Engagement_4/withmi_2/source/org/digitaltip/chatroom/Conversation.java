package org.digitaltip.chatroom;

import org.digitaltip.dialogs.TalkersNetworkAddress;
import org.digitaltip.dialogs.TalkersPublicIdentity;
import org.digitaltip.mathematic.CryptoSystemPublicKey;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A chat room
 */
public class Conversation {
    // max number of characters in a chat name
    public final static int MAX_NAME_LENGTH = 19;

    // null user
    public final static TalkersPublicIdentity NULL_IDENTITY = new TalkersPublicIdentity("null",
            new CryptoSystemPublicKey(new BigInteger("65537"), new BigInteger("11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")),
            new TalkersNetworkAddress("localhost", -1000));

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
    private final Set<User> customers = new HashSet<>();

    private final List<String> unreadMessages = new ArrayList<>();

    private final HangIn withMi;

    public Conversation(HangIn withMi, String name, String uniqueId) {
        this.withMi = withMi;
        this.name = name;
        this.uniqueId = uniqueId;
    }

    /**
     * @return name of chat
     */
    public String getName() {
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

    public String obtainUniqueId() {
        return uniqueId;
    }

    public void addCustomer(User customer) {
        if (canAddMoreCustomers()) {
            customers.add(customer);
        } else {
            withMi.printCustomerMsg("Not adding user " + customer.takeName() + " because " + name +
                    " has reached the maximum number of users");
        }
    }

    public List<User> takeCustomers() {
        List<User> sortedCustomers = new ArrayList<>(customers);
        Collections.sort(sortedCustomers);
        return sortedCustomers;
    }

    public boolean containsCustomer(User customer) {
        return customers.contains(customer);
    }

    public void removeCustomer(User customer) {
        customers.remove(customer);
    }

    public boolean canAddMoreCustomers() {
        return customers.size() < MAX_NUM_OF_USERS;
    }

    public void storeUnreadMessage(String message) {
        unreadMessages.add(message);
    }
}
