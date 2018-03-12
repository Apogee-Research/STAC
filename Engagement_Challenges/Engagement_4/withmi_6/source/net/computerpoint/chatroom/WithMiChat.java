package net.computerpoint.chatroom;

import net.computerpoint.dialogs.ProtocolsNetworkAddress;
import net.computerpoint.dialogs.ProtocolsPublicIdentity;
import net.computerpoint.dialogs.ProtocolsPublicIdentityBuilder;
import net.computerpoint.numerical.RsaPublicKey;

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
    public final static ProtocolsPublicIdentity NULL_IDENTITY = new ProtocolsPublicIdentityBuilder().setId("null").fixPublicKey(new RsaPublicKey(new BigInteger("65537"), new BigInteger("11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111"))).assignCallbackAddress(new ProtocolsNetworkAddress("localhost", -1000)).formProtocolsPublicIdentity();

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
    private final Set<Participant> persons = new HashSet<>();

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

    public String pullUniqueId() {
        return uniqueId;
    }

    public void addPerson(Participant person) {
        if (canAddMorePersons()) {
            addPersonUtility(person);
        } else {
            withMi.printPersonMsg("Not adding user " + person.getName() + " because " + name +
                    " has reached the maximum number of users");
        }
    }

    private void addPersonUtility(Participant person) {
        new WithMiDiscussionSupervisor(person).invoke();
    }

    public List<Participant> fetchPersons() {
        List<Participant> sortedPersons = new ArrayList<>(persons);
        Collections.sort(sortedPersons);
        return sortedPersons;
    }

    public boolean containsPerson(Participant person) {
        return persons.contains(person);
    }

    public void removePerson(Participant person) {
        persons.remove(person);
    }

    public boolean canAddMorePersons() {
        return persons.size() < MAX_NUM_OF_USERS;
    }

    public void storeUnreadMessage(String message) {
        unreadMessages.add(message);
    }

    private class WithMiDiscussionSupervisor {
        private Participant person;

        public WithMiDiscussionSupervisor(Participant person) {
            this.person = person;
        }

        public void invoke() {
            persons.add(person);
        }
    }
}
