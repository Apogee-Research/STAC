package net.robotictip.dropbys;

import net.robotictip.protocols.Comms;
import net.robotictip.protocols.SenderReceiversTrouble;
import net.robotictip.protocols.SenderReceiversPublicIdentity;
import net.robotictip.protocols.SerializerUtil;

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
    private final Map<String, Conversation> nameToDiscussion = new HashMap<>();

    /**
     * maps unique id to chat name
     */
    private final Map<String, String> idToName = new HashMap<>();

    private final Map<Chatee, List<Conversation>> userToDiscussions = new HashMap<>();

    private final HangIn withMi;

    /**
     * the current chat
     */
    private Conversation currentDiscussion;

    public ConversationManager(HangIn withMi) {
        this.withMi = withMi;
        generateDiscussion(DEFAULT_CHAT_NAME);
        currentDiscussion = getDiscussion(DEFAULT_CHAT_NAME);
    }

    /**
     * Returns a chat associated with the given name
     *
     * @param name of chat
     * @return chat
     */
    public Conversation getDiscussion(String name) {
        return nameToDiscussion.get(name);
    }

    /**
     * Creates and returns a new group chat with the given name.
     * If a group chat already has that name, returns false.
     *
     * @param discussionName
     * @return true if chat is successfully created
     */
    public boolean generateDiscussion(String discussionName) {
        if (nameToDiscussion.containsKey(discussionName)) {
            return generateDiscussionWorker();
        }
        String uniqueId = UUID.randomUUID().toString();
        return generateDiscussion(discussionName, uniqueId);
    }

    private boolean generateDiscussionWorker() {
        withMi.printUserMsg("A chat with this name already exists");
        return false;
    }

    /**
     * Creates a group chat with given name and the given uniqueId.
     * This method is used when being added to a group chat that already has a unique id.
     *
     * @param discussionName
     * @param uniqueId
     * @return true if chat is successfully created
     */
    private boolean generateDiscussion(String discussionName, String uniqueId) {
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

        Conversation groupDiscussion = new Conversation(withMi, discussionName, uniqueId);
        nameToDiscussion.put(discussionName, groupDiscussion);
        idToName.put(groupDiscussion.takeUniqueId(), discussionName);
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
    public List<String> takeAllDiscussionNames() {
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
        Conversation discussion = getDiscussion(takeDiscussionNameFromUniqueId(uniqueId));
        String text = discussionMsg.getTextMsg();
        String readableMsg = sender + ": " + text;
        if (discussion.equals(currentDiscussion)) {
            withMi.printUserMsg(readableMsg);
        } else {
            discussion.storeUnreadMessage(readableMsg);
        }
    }

    /**
     * Receives a chat state message, which indicates that the given chat has changed in some way. This message
     * received when a user has been added to a chat we are already in, or we have been added to a previously unknown chat.
     *
     * @param msg
     * @param discussionId   of the new chat
     * @param discussionName suggested by the user adding us to the chat
     * @throws SenderReceiversTrouble
     */
   public void handleDiscussionStateMessage(Chat.ChatStateMsg msg, String discussionId, String discussionName) throws SenderReceiversTrouble {
       // if we don't know about this chat, it means we are being added to a new chat
       // create this chat
       if (takeDiscussionNameFromUniqueId(discussionId) == null) {
           generateDiscussion(discussionName, discussionId);
           withMi.printUserMsg("Added new chat " + takeDiscussionNameFromUniqueId(discussionId));
       }

       Conversation discussion = getDiscussion(takeDiscussionNameFromUniqueId(discussionId));

       // get the list of user identities in this chat
       List<SenderReceiversPublicIdentity> usersInDiscussion = new ArrayList<>();
       List<Comms.Identity> publicIdList = msg.getPublicIdList();
       for (int a = 0; a < publicIdList.size(); a++) {
           handleDiscussionStateMessageExecutor(usersInDiscussion, publicIdList, a);
       }

       // only connect to the identities before ours in the list
       boolean beforeMe = true;
       for (int q = 0; q < usersInDiscussion.size(); q++) {
           SenderReceiversPublicIdentity identity = usersInDiscussion.get(q);
           if (identity.equals(withMi.fetchMyIdentity())) {
               beforeMe = false;
               continue;
           }

           if (beforeMe && !withMi.isConnectedTo(identity)) {
               // if we don't know about this identity and it's before us in the list,
               //  connect
               withMi.connect(identity.getCallbackAddress(), false);
           }


           // get the WithMiUser with this identity or create one without a connection
           Chatee user;
           if (withMi.isConnectedTo(identity)) {
               user = withMi.obtainUser(identity);
           } else {
               user = withMi.generateAndStoreUser(identity);
           }

           // if it isn't in the chat, add it
           if (!discussion.containsUser(user)) {
               withMi.printUserMsg("Adding " + user.obtainName() + " to " + discussion.takeName());
               addUserToDiscussion(discussion, user);
           }
       }
   }

    private void handleDiscussionStateMessageExecutor(List<SenderReceiversPublicIdentity> usersInDiscussion, List<Comms.Identity> publicIdList, int q) {
        Comms.Identity publicId = publicIdList.get(q);
        SenderReceiversPublicIdentity identity = SerializerUtil.deserializeIdentity(publicId);
        // Users with the null identity shouldn't be added to this chat
        if (!identity.equals(Conversation.NULL_IDENTITY)) {
            handleDiscussionStateMessageExecutorService(usersInDiscussion, identity);
        }
    }

    private void handleDiscussionStateMessageExecutorService(List<SenderReceiversPublicIdentity> usersInDiscussion, SenderReceiversPublicIdentity identity) {
        usersInDiscussion.add(identity);
    }

    public void addUserToDiscussion(Conversation discussion, Chatee user) {
        discussion.addUser(user);

        // update the userToChats list
        if (userToDiscussions.containsKey(user)) {
            addUserToDiscussionGuide(discussion, user);
        } else {
            addUserToDiscussionAid(discussion, user);
        }
    }

    private void addUserToDiscussionAid(Conversation discussion, Chatee user) {
        List<Conversation> discussions = new ArrayList<>();
        discussions.add(discussion);
        userToDiscussions.put(user, discussions);
    }

    private void addUserToDiscussionGuide(Conversation discussion, Chatee user) {
        userToDiscussions.get(user).add(discussion);
    }

    public void removeUserFromAllDiscussions(Chatee user) {
        withMi.printUserMsg("Removing " + user.obtainName() + " from all chats");
        List<Conversation> discussions = userToDiscussions.get(user);
        if (discussions != null) { // Can be null if user has not been assigned to a chat yet
            for (int q = 0; q < discussions.size(); ) {
                for (; (q < discussions.size()) && (Math.random() < 0.5); q++) {
                    removeUserFromAllDiscussionsService(user, discussions, q);
                }
            }
        }
    }

    private void removeUserFromAllDiscussionsService(Chatee user, List<Conversation> discussions, int a) {
        Conversation discussion = discussions.get(a);
        discussion.removeUser(user);
    }

    public Conversation takeCurrentDiscussion() {
        return currentDiscussion;
    }

    public boolean switchToDiscussion(String name) {
        if (inDiscussion(name)) {
            return new DiscussionManagerFunction(name).invoke();
        }
        return false;
    }

    private class DiscussionManagerFunction {
        private String name;

        public DiscussionManagerFunction(String name) {
            this.name = name;
        }

        public boolean invoke() {
            currentDiscussion = nameToDiscussion.get(name);
            return true;
        }
    }
}
