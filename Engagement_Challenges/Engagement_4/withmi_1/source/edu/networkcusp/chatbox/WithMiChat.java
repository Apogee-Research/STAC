package edu.networkcusp.chatbox;

import edu.networkcusp.protocols.CommunicationsNetworkAddress;
import edu.networkcusp.protocols.CommunicationsPublicIdentity;
import edu.networkcusp.math.CryptoPublicKey;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A chat room
 */
public class WithMiChat {
    // max number of characters in a chat name
    public final static int MAX_NAME_LENGTH = 19;

    // null user
    public final static CommunicationsPublicIdentity NULL_IDENTITY = new CommunicationsPublicIdentity("null",
            new CryptoPublicKey(new BigInteger("65537"), new BigInteger("11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")),
            new CommunicationsNetworkAddress("localhost", -1000));

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
    private final Set<WithMiUser> members = new HashSet<>();

    private final List<String> unreadMessages = new ArrayList<>();

    private final HangIn withMi;

    public WithMiChat(HangIn withMi, String name, String uniqueId) {
        this.withMi = withMi;
        this.name = name;
        this.uniqueId = uniqueId;
    }

    /**
     * @return name of chat
     */
    public String grabName() {
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

    public void addMember(WithMiUser member) {
        if (canAddMoreMembers()) {
            members.add(member);
        } else {
            addMemberSupervisor(member);
        }
    }

    private void addMemberSupervisor(WithMiUser member) {
        withMi.printMemberMsg("Not adding user " + member.obtainName() + " because " + name +
                " has reached the maximum number of users");
    }

    public List<WithMiUser> getMembers() {
        List<WithMiUser> sortedMembers = new ArrayList<>(members);
        Collections.sort(sortedMembers);
        return sortedMembers;
    }

    public boolean containsMember(WithMiUser member) {
        return members.contains(member);
    }

    public void removeMember(WithMiUser member) {
        members.remove(member);
    }

    public boolean canAddMoreMembers() {
        return members.size() < MAX_NUM_OF_USERS;
    }

    public void storeUnreadMessage(String message) {
        unreadMessages.add(message);
    }
}
