package org.digitaltip.chatroom;

import org.digitaltip.dialogs.Comms;
import org.digitaltip.dialogs.TalkersDeviation;
import org.digitaltip.dialogs.TalkersPublicIdentity;
import org.digitaltip.dialogs.SerializerUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages the user's chats
 */
public class ConversationManager {
    private static final String DEFAULT_CHAT_NAME = "myFirstChat";

    /**
     * maps chat name to chat
     */
    private final Map<String, Conversation> nameToConference = new HashMap<>();

    /**
     * maps unique id to chat name
     */
    private final Map<String, String> idToName = new HashMap<>();

    private final Map<User, List<Conversation>> customerToConferences = new HashMap<>();

    private final HangIn withMi;

    /**
     * the current chat
     */
    private Conversation currentConference;

    public ConversationManager(HangIn withMi) {
        this.withMi = withMi;
        makeConference(DEFAULT_CHAT_NAME);
        currentConference = pullConference(DEFAULT_CHAT_NAME);
    }

    /**
     * Returns a chat associated with the given name
     *
     * @param name of chat
     * @return chat
     */
    public Conversation pullConference(String name) {
        return nameToConference.get(name);
    }

    /**
     * Creates and returns a new group chat with the given name.
     * If a group chat already has that name, returns false.
     *
     * @param conferenceName
     * @return true if chat is successfully created
     */
    public boolean makeConference(String conferenceName) {
        if (nameToConference.containsKey(conferenceName)) {
            withMi.printCustomerMsg("A chat with this name already exists");
            return false;
        }
        String uniqueId = UUID.randomUUID().toString();
        return makeConference(conferenceName, uniqueId);
    }

    /**
     * Creates a group chat with given name and the given uniqueId.
     * This method is used when being added to a group chat that already has a unique id.
     *
     * @param conferenceName
     * @param uniqueId
     * @return true if chat is successfully created
     */
    private boolean makeConference(String conferenceName, String uniqueId) {
        if (nameToConference.containsKey(conferenceName)) {
            // already have a user with this name, we need to give them some different identifier...
            int suffixId = 1;
            String newName = conferenceName + "_" + Integer.toHexString(suffixId);
            while (nameToConference.containsKey(newName)) {
                suffixId++;
                newName = conferenceName + "_" + Integer.toHexString(suffixId);
            }
            conferenceName = newName;
        }

        Conversation groupConference = new Conversation(withMi, conferenceName, uniqueId);
        nameToConference.put(conferenceName, groupConference);
        idToName.put(groupConference.obtainUniqueId(), conferenceName);
        currentConference = groupConference;
        return true;
    }

    /**
     * @param name of chat
     * @return true if we are currently in the chat with the given name
     */
    public boolean inConference(String name) {
        return nameToConference.containsKey(name);
    }

    /**
     * @param uniqueId of chat
     * @return name of chat associated with the given id
     */
    public String obtainConferenceNameFromUniqueId(String uniqueId) {
        return idToName.get(uniqueId);
    }

    /**
     * @return names of all the chats we're in
     */
    public List<String> pullAllConferenceNames() {
        return new ArrayList<>(nameToConference.keySet());
    }

    /**
     * Receives a chat message and decides where to put it. If we are currently in the chat associated with
     * the message, this prints the message. If we are in a different chat, this stores the unread message for when we
     * switch to that chat.
     *
     * @param sender   of the message
     * @param conferenceMsg
     * @param uniqueId of chat associated with the message
     */
    public void handleConferenceMessage(String sender, Chat.ChatMsg conferenceMsg, String uniqueId) {
        Conversation conference = pullConference(obtainConferenceNameFromUniqueId(uniqueId));
        String text = conferenceMsg.getTextMsg();
        String readableMsg = sender + ": " + text;
        if (conference.equals(currentConference)) {
            handleConferenceMessageTarget(readableMsg);
        } else {
            handleConferenceMessageAid(conference, readableMsg);
        }
    }

    private void handleConferenceMessageAid(Conversation conference, String readableMsg) {
        conference.storeUnreadMessage(readableMsg);
    }

    private void handleConferenceMessageTarget(String readableMsg) {
        withMi.printCustomerMsg(readableMsg);
    }

    /**
     * Receives a chat state message, which indicates that the given chat has changed in some way. This message
     * received when a user has been added to a chat we are already in, or we have been added to a previously unknown chat.
     *
     * @param msg
     * @param conferenceId   of the new chat
     * @param conferenceName suggested by the user adding us to the chat
     * @throws TalkersDeviation
     */
   public void handleConferenceStateMessage(Chat.ChatStateMsg msg, String conferenceId, String conferenceName) throws TalkersDeviation {
       // if we don't know about this chat, it means we are being added to a new chat
       // create this chat
       if (obtainConferenceNameFromUniqueId(conferenceId) == null) {
           handleConferenceStateMessageFunction(conferenceId, conferenceName);
       }

       Conversation conference = pullConference(obtainConferenceNameFromUniqueId(conferenceId));

       // get the list of user identities in this chat
       List<TalkersPublicIdentity> customersInConference = new ArrayList<>();
       List<Comms.Identity> publicIdList = msg.getPublicIdList();
       for (int i = 0; i < publicIdList.size(); i++) {
           Comms.Identity publicId = publicIdList.get(i);
           TalkersPublicIdentity identity = SerializerUtil.deserializeIdentity(publicId);
           // Users with the null identity shouldn't be added to this chat
           if (!identity.equals(Conversation.NULL_IDENTITY)) {
               handleConferenceStateMessageService(customersInConference, identity);
           }
       }

       // only connect to the identities before ours in the list
       boolean beforeMe = true;
       for (int b = 0; b < customersInConference.size(); b++) {
           TalkersPublicIdentity identity = customersInConference.get(b);
           if (identity.equals(withMi.obtainMyIdentity())) {
               beforeMe = false;
               continue;
           }

           if (beforeMe && !withMi.isConnectedTo(identity)) {
               // if we don't know about this identity and it's before us in the list,
               //  connect
               withMi.connect(identity.grabCallbackAddress(), false);
           }


           // get the WithMiUser with this identity or create one without a connection
           User customer;
           if (withMi.isConnectedTo(identity)) {
               customer = withMi.getCustomer(identity);
           } else {
               customer = withMi.makeAndStoreCustomer(identity);
           }

           // if it isn't in the chat, add it
           if (!conference.containsCustomer(customer)) {
               withMi.printCustomerMsg("Adding " + customer.takeName() + " to " + conference.getName());
               addCustomerToConference(conference, customer);
           }
       }
   }

    private void handleConferenceStateMessageService(List<TalkersPublicIdentity> customersInConference, TalkersPublicIdentity identity) {
        customersInConference.add(identity);
    }

    private void handleConferenceStateMessageFunction(String conferenceId, String conferenceName) {
        makeConference(conferenceName, conferenceId);
        withMi.printCustomerMsg("Added new chat " + obtainConferenceNameFromUniqueId(conferenceId));
    }

    public void addCustomerToConference(Conversation conference, User customer) {
        conference.addCustomer(customer);

        // update the userToChats list
        if (customerToConferences.containsKey(customer)) {
            addCustomerToConferenceCoordinator(conference, customer);
        } else {
            List<Conversation> conferences = new ArrayList<>();
            conferences.add(conference);
            customerToConferences.put(customer, conferences);
        }
    }

    private void addCustomerToConferenceCoordinator(Conversation conference, User customer) {
        customerToConferences.get(customer).add(conference);
    }

    public void removeCustomerFromAllConferences(User customer) {
        withMi.printCustomerMsg("Removing " + customer.takeName() + " from all chats");
        List<Conversation> conferences = customerToConferences.get(customer);
        if (conferences != null) { // Can be null if user has not been assigned to a chat yet
            for (int q = 0; q < conferences.size(); q++) {
                Conversation conference = conferences.get(q);
                conference.removeCustomer(customer);
            }
        }
    }

    public Conversation obtainCurrentConference() {
        return currentConference;
    }

    public boolean switchToConference(String name) {
        if (inConference(name)) {
            currentConference = nameToConference.get(name);
            return true;
        }
        return false;
    }
}
