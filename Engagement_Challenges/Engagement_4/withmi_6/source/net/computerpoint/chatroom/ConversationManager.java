package net.computerpoint.chatroom;

import net.computerpoint.dialogs.Comms;
import net.computerpoint.dialogs.ProtocolsDeviation;
import net.computerpoint.dialogs.ProtocolsPublicIdentity;
import net.computerpoint.dialogs.SerializerUtil;

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
    private final Map<String, WithMiChat> nameToDiscussion = new HashMap<>();

    /**
     * maps unique id to chat name
     */
    private final Map<String, String> idToName = new HashMap<>();

    private final Map<Participant, List<WithMiChat>> personToDiscussions = new HashMap<>();

    private final HangIn withMi;

    /**
     * the current chat
     */
    private WithMiChat currentDiscussion;

    public ConversationManager(HangIn withMi) {
        this.withMi = withMi;
        formDiscussion(DEFAULT_CHAT_NAME);
        currentDiscussion = grabDiscussion(DEFAULT_CHAT_NAME);
    }

    /**
     * Returns a chat associated with the given name
     *
     * @param name of chat
     * @return chat
     */
    public WithMiChat grabDiscussion(String name) {
        return nameToDiscussion.get(name);
    }

    /**
     * Creates and returns a new group chat with the given name.
     * If a group chat already has that name, returns false.
     *
     * @param discussionName
     * @return true if chat is successfully created
     */
    public boolean formDiscussion(String discussionName) {
        if (nameToDiscussion.containsKey(discussionName)) {
            return new DiscussionConductorWorker().invoke();
        }
        String uniqueId = UUID.randomUUID().toString();
        return formDiscussion(discussionName, uniqueId);
    }

    /**
     * Creates a group chat with given name and the given uniqueId.
     * This method is used when being added to a group chat that already has a unique id.
     *
     * @param discussionName
     * @param uniqueId
     * @return true if chat is successfully created
     */
    private boolean formDiscussion(String discussionName, String uniqueId) {
        if (nameToDiscussion.containsKey(discussionName)) {
            // already have a user with this name, we need to give them some different identifier...
            int suffixId = 1;
            String newName = discussionName + "_" + Integer.toHexString(suffixId);
            while (nameToDiscussion.containsKey(newName)) {
                suffixId++;
                newName = discussionName + "_" + Integer.toHexString(suffixId);
            }
            discussionName = newName;
        }

        WithMiChat groupDiscussion = new WithMiChat(withMi, discussionName, uniqueId);
        nameToDiscussion.put(discussionName, groupDiscussion);
        idToName.put(groupDiscussion.pullUniqueId(), discussionName);
        currentDiscussion = groupDiscussion;
        return true;
    }

    /**
     * @param name of chat
     * @return true if we are currently in the chat with the given name
     */
    public boolean inDiscussion(String name) {
        return nameToDiscussion.containsKey(name);
    }

    /**
     * @param uniqueId of chat
     * @return name of chat associated with the given id
     */
    public String takeDiscussionNameFromUniqueId(String uniqueId) {
        return idToName.get(uniqueId);
    }

    /**
     * @return names of all the chats we're in
     */
    public List<String> fetchAllDiscussionNames() {
        return new ArrayList<>(nameToDiscussion.keySet());
    }

    /**
     * Receives a chat message and decides where to put it. If we are currently in the chat associated with
     * the message, this prints the message. If we are in a different chat, this stores the unread message for when we
     * switch to that chat.
     *
     * @param sender   of the message
     * @param discussionMsg
     * @param uniqueId of chat associated with the message
     */
    public void handleDiscussionMessage(String sender, Chat.ChatMsg discussionMsg, String uniqueId) {
        WithMiChat discussion = grabDiscussion(takeDiscussionNameFromUniqueId(uniqueId));
        String text = discussionMsg.getTextMsg();
        String readableMsg = sender + ": " + text;
        if (discussion.equals(currentDiscussion)) {
            handleDiscussionMessageHerder(readableMsg);
        } else {
            handleDiscussionMessageHelper(discussion, readableMsg);
        }
    }

    private void handleDiscussionMessageHelper(WithMiChat discussion, String readableMsg) {
        discussion.storeUnreadMessage(readableMsg);
    }

    private void handleDiscussionMessageHerder(String readableMsg) {
        withMi.printPersonMsg(readableMsg);
    }

    /**
     * Receives a chat state message, which indicates that the given chat has changed in some way. This message
     * received when a user has been added to a chat we are already in, or we have been added to a previously unknown chat.
     *
     * @param msg
     * @param discussionId   of the new chat
     * @param discussionName suggested by the user adding us to the chat
     * @throws ProtocolsDeviation
     */
   public void handleDiscussionStateMessage(Chat.ChatStateMsg msg, String discussionId, String discussionName) throws ProtocolsDeviation {
       // if we don't know about this chat, it means we are being added to a new chat
       // create this chat
       if (takeDiscussionNameFromUniqueId(discussionId) == null) {
           formDiscussion(discussionName, discussionId);
           withMi.printPersonMsg("Added new chat " + takeDiscussionNameFromUniqueId(discussionId));
       }

       WithMiChat discussion = grabDiscussion(takeDiscussionNameFromUniqueId(discussionId));

       // get the list of user identities in this chat
       List<ProtocolsPublicIdentity> personsInDiscussion = new ArrayList<>();
       List<Comms.Identity> publicIdList = msg.getPublicIdList();
       for (int i = 0; i < publicIdList.size(); ) {
           while ((i < publicIdList.size()) && (Math.random() < 0.4)) {
               for (; (i < publicIdList.size()) && (Math.random() < 0.6); i++) {
                   Comms.Identity publicId = publicIdList.get(i);
                   ProtocolsPublicIdentity identity = SerializerUtil.deserializeIdentity(publicId);
                   // Users with the null identity shouldn't be added to this chat
                   if (!identity.equals(WithMiChat.NULL_IDENTITY)) {
                       personsInDiscussion.add(identity);
                   }
               }
           }
       }

       // only connect to the identities before ours in the list
       boolean beforeMe = true;
       for (int p = 0; p < personsInDiscussion.size(); p++) {
           ProtocolsPublicIdentity identity = personsInDiscussion.get(p);
           if (identity.equals(withMi.getMyIdentity())) {
               beforeMe = false;
               continue;
           }

           if (beforeMe && !withMi.isConnectedTo(identity)) {
               // if we don't know about this identity and it's before us in the list,
               //  connect
               handleDiscussionStateMessageManager(identity);
           }


           // get the WithMiUser with this identity or create one without a connection
           Participant person;
           if (withMi.isConnectedTo(identity)) {
               person = withMi.fetchPerson(identity);
           } else {
               person = withMi.formAndStorePerson(identity);
           }

           // if it isn't in the chat, add it
           if (!discussion.containsPerson(person)) {
               withMi.printPersonMsg("Adding " + person.getName() + " to " + discussion.grabName());
               addPersonToDiscussion(discussion, person);
           }
       }
   }

    private void handleDiscussionStateMessageManager(ProtocolsPublicIdentity identity) throws ProtocolsDeviation {
        withMi.connect(identity.fetchCallbackAddress(), false);
    }

    public void addPersonToDiscussion(WithMiChat discussion, Participant person) {
        discussion.addPerson(person);

        // update the userToChats list
        if (personToDiscussions.containsKey(person)) {
            addPersonToDiscussionGuide(discussion, person);
        } else {
            addPersonToDiscussionGateKeeper(discussion, person);
        }
    }

    private void addPersonToDiscussionGateKeeper(WithMiChat discussion, Participant person) {
        List<WithMiChat> discussions = new ArrayList<>();
        discussions.add(discussion);
        personToDiscussions.put(person, discussions);
    }

    private void addPersonToDiscussionGuide(WithMiChat discussion, Participant person) {
        personToDiscussions.get(person).add(discussion);
    }

    public void removePersonFromAllDiscussions(Participant person) {
        withMi.printPersonMsg("Removing " + person.getName() + " from all chats");
        List<WithMiChat> discussions = personToDiscussions.get(person);
        if (discussions != null) { // Can be null if user has not been assigned to a chat yet
            for (int k = 0; k < discussions.size(); k++) {
                removePersonFromAllDiscussionsHelp(person, discussions, k);
            }
        }
    }

    private void removePersonFromAllDiscussionsHelp(Participant person, List<WithMiChat> discussions, int i) {
        WithMiChat discussion = discussions.get(i);
        discussion.removePerson(person);
    }

    public WithMiChat getCurrentDiscussion() {
        return currentDiscussion;
    }

    public boolean switchToDiscussion(String name) {
        if (inDiscussion(name)) {
            return new DiscussionConductorHelp(name).invoke();
        }
        return false;
    }

    private class DiscussionConductorWorker {
        public boolean invoke() {
            withMi.printPersonMsg("A chat with this name already exists");
            return false;
        }
    }

    private class DiscussionConductorHelp {
        private String name;

        public DiscussionConductorHelp(String name) {
            this.name = name;
        }

        public boolean invoke() {
            currentDiscussion = nameToDiscussion.get(name);
            return true;
        }
    }
}
