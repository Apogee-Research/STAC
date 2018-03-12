package net.robotictip.dropbys;

import net.robotictip.protocols.SenderReceiversPublicIdentity;
import net.robotictip.numerical.RsaPublicKey;
import net.robotictip.protocols.SenderReceiversNetworkAddressBuilder;

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
    public final static SenderReceiversPublicIdentity NULL_IDENTITY = new SenderReceiversPublicIdentity("null",
            new RsaPublicKey(new BigInteger("65537"), new BigInteger("11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")),
            new SenderReceiversNetworkAddressBuilder().assignHome("localhost").definePort(-1000).generateSenderReceiversNetworkAddress());

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
    private final Set<Chatee> users = new HashSet<>();

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
    public String takeName() {
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

    public String takeUniqueId() {
        return uniqueId;
    }

    public void addUser(Chatee user) {
        if (canAddMoreUsers()) {
            users.add(user);
        } else {
            withMi.printUserMsg("Not adding user " + user.obtainName() + " because " + name +
                    " has reached the maximum number of users");
        }
    }

    public List<Chatee> obtainUsers() {
        List<Chatee> sortedUsers = new ArrayList<>(users);
        Collections.sort(sortedUsers);
        return sortedUsers;
    }

    public boolean containsUser(Chatee user) {
        return users.contains(user);
    }

    public void removeUser(Chatee user) {
        users.remove(user);
    }

    public boolean canAddMoreUsers() {
        return users.size() < MAX_NUM_OF_USERS;
    }

    public void storeUnreadMessage(String message) {
        unreadMessages.add(message);
    }
}
