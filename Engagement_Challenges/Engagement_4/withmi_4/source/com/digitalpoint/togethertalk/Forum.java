package com.digitalpoint.togethertalk;

import com.digitalpoint.dialogs.SenderReceiversNetworkAddress;
import com.digitalpoint.dialogs.SenderReceiversPublicIdentity;
import com.digitalpoint.math.CryptoPublicKey;

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
    public final static SenderReceiversPublicIdentity NULL_IDENTITY = new SenderReceiversPublicIdentity("null",
            new CryptoPublicKey(new BigInteger("65537"), new BigInteger("11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")),
            new SenderReceiversNetworkAddress("localhost", -1000));

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
    private final Set<Participant> members = new HashSet<>();

    private final List<String> unreadMessages = new ArrayList<>();

    private final HangIn withMi;

    public Forum(HangIn withMi, String name, String uniqueId) {
        this.withMi = withMi;
        this.name = name;
        this.uniqueId = uniqueId;
    }

    /**
     * @return name of chat
     */
    public String pullName() {
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

    public String pullUniqueId() {
        return uniqueId;
    }

    public void addMember(Participant member) {
        if (canAddMoreMembers()) {
            addMemberCoach(member);
        } else {
            withMi.printMemberMsg("Not adding user " + member.grabName() + " because " + name +
                    " has reached the maximum number of users");
        }
    }

    private void addMemberCoach(Participant member) {
        members.add(member);
    }

    public List<Participant> pullMembers() {
        List<Participant> sortedMembers = new ArrayList<>(members);
        Collections.sort(sortedMembers);
        return sortedMembers;
    }

    public boolean containsMember(Participant member) {
        return members.contains(member);
    }

    public void removeMember(Participant member) {
        members.remove(member);
    }

    public boolean canAddMoreMembers() {
        return members.size() < MAX_NUM_OF_USERS;
    }

    public void storeUnreadMessage(String message) {
        unreadMessages.add(message);
    }
}
