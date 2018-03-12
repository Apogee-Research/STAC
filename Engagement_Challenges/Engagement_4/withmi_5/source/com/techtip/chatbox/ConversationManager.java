package com.techtip.chatbox;

import com.techtip.communications.Comms;
import com.techtip.communications.DialogsDeviation;
import com.techtip.communications.DialogsPublicIdentity;
import com.techtip.communications.SerializerUtil;

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
    private final Map<String, Forum> nameToForum = new HashMap<>();

    /**
     * maps unique id to chat name
     */
    private final Map<String, String> idToName = new HashMap<>();

    private final Map<WithMiUser, List<Forum>> customerToForums = new HashMap<>();

    private final DropBy withMi;

    /**
     * the current chat
     */
    private Forum currentForum;

    public ConversationManager(DropBy withMi) {
        this.withMi = withMi;
        formForum(DEFAULT_CHAT_NAME);
        currentForum = takeForum(DEFAULT_CHAT_NAME);
    }

    /**
     * Returns a chat associated with the given name
     *
     * @param name of chat
     * @return chat
     */
    public Forum takeForum(String name) {
        return nameToForum.get(name);
    }

    /**
     * Creates and returns a new group chat with the given name.
     * If a group chat already has that name, returns false.
     *
     * @param forumName
     * @return true if chat is successfully created
     */
    public boolean formForum(String forumName) {
        if (nameToForum.containsKey(forumName)) {
            return formForumAssist();
        }
        String uniqueId = UUID.randomUUID().toString();
        return formForum(forumName, uniqueId);
    }

    private boolean formForumAssist() {
        withMi.printCustomerMsg("A chat with this name already exists");
        return false;
    }

    /**
     * Creates a group chat with given name and the given uniqueId.
     * This method is used when being added to a group chat that already has a unique id.
     *
     * @param forumName
     * @param uniqueId
     * @return true if chat is successfully created
     */
    private boolean formForum(String forumName, String uniqueId) {
        if (nameToForum.containsKey(forumName)) {
            // already have a user with this name, we need to give them some different identifier...
            int suffixId = 1;
            String newName = forumName + "_" + Integer.toHexString(suffixId);
            while (nameToForum.containsKey(newName)) {
                suffixId++;
                newName = forumName + "_" + Integer.toHexString(suffixId);
            }
            forumName = newName;
        }

        Forum groupForum = new Forum(withMi, forumName, uniqueId);
        nameToForum.put(forumName, groupForum);
        idToName.put(groupForum.getUniqueId(), forumName);
        currentForum = groupForum;
        return true;
    }

    /**
     * @param name of chat
     * @return true if we are currently in the chat with the given name
     */
    public boolean inForum(String name) {
        return nameToForum.containsKey(name);
    }

    /**
     * @param uniqueId of chat
     * @return name of chat associated with the given id
     */
    public String getForumNameFromUniqueId(String uniqueId) {
        return idToName.get(uniqueId);
    }

    /**
     * @return names of all the chats we're in
     */
    public List<String> pullAllForumNames() {
        return new ArrayList<>(nameToForum.keySet());
    }

    /**
     * Receives a chat message and decides where to put it. If we are currently in the chat associated with
     * the message, this prints the message. If we are in a different chat, this stores the unread message for when we
     * switch to that chat.
     *
     * @param sender   of the message
     * @param forumMsg
     * @param uniqueId of chat associated with the message
     */
    public void handleForumMessage(String sender, Chat.ChatMsg forumMsg, String uniqueId) {
        Forum forum = takeForum(getForumNameFromUniqueId(uniqueId));
        String text = forumMsg.getTextMsg();
        String readableMsg = sender + ": " + text;
        if (forum.equals(currentForum)) {
            withMi.printCustomerMsg(readableMsg);
        } else {
            handleForumMessageHelper(forum, readableMsg);
        }
    }

    private void handleForumMessageHelper(Forum forum, String readableMsg) {
        forum.storeUnreadMessage(readableMsg);
    }

    /**
     * Receives a chat state message, which indicates that the given chat has changed in some way. This message
     * received when a user has been added to a chat we are already in, or we have been added to a previously unknown chat.
     *
     * @param msg
     * @param forumId   of the new chat
     * @param forumName suggested by the user adding us to the chat
     * @throws DialogsDeviation
     */
   public void handleForumStateMessage(Chat.ChatStateMsg msg, String forumId, String forumName) throws DialogsDeviation {
       // if we don't know about this chat, it means we are being added to a new chat
       // create this chat
       if (getForumNameFromUniqueId(forumId) == null) {
           formForum(forumName, forumId);
           withMi.printCustomerMsg("Added new chat " + getForumNameFromUniqueId(forumId));
       }

       Forum forum = takeForum(getForumNameFromUniqueId(forumId));

       // get the list of user identities in this chat
       List<DialogsPublicIdentity> customersInForum = new ArrayList<>();
       List<Comms.Identity> publicIdList = msg.getPublicIdList();
       for (int j = 0; j < publicIdList.size(); j++) {
           handleForumStateMessageHerder(customersInForum, publicIdList, j);
       }

       // only connect to the identities before ours in the list
       boolean beforeMe = true;
       for (int c = 0; c < customersInForum.size(); c++) {
           DialogsPublicIdentity identity = customersInForum.get(c);
           if (identity.equals(withMi.getMyIdentity())) {
               beforeMe = false;
               continue;
           }

           if (beforeMe && !withMi.isConnectedTo(identity)) {
               // if we don't know about this identity and it's before us in the list,
               //  connect
               withMi.connect(identity.fetchCallbackAddress(), false);
           }


           // get the WithMiUser with this identity or create one without a connection
           WithMiUser customer;
           if (withMi.isConnectedTo(identity)) {
               customer = withMi.getCustomer(identity);
           } else {
               customer = withMi.formAndStoreCustomer(identity);
           }

           // if it isn't in the chat, add it
           if (!forum.containsCustomer(customer)) {
               handleForumStateMessageAssist(forum, customer);
           }
       }
   }

    private void handleForumStateMessageAssist(Forum forum, WithMiUser customer) {
        withMi.printCustomerMsg("Adding " + customer.pullName() + " to " + forum.obtainName());
        addCustomerToForum(forum, customer);
    }

    private void handleForumStateMessageHerder(List<DialogsPublicIdentity> customersInForum, List<Comms.Identity> publicIdList, int q) {
        Comms.Identity publicId = publicIdList.get(q);
        DialogsPublicIdentity identity = SerializerUtil.deserializeIdentity(publicId);
        // Users with the null identity shouldn't be added to this chat
        if (!identity.equals(Forum.NULL_IDENTITY)) {
            customersInForum.add(identity);
        }
    }

    public void addCustomerToForum(Forum forum, WithMiUser customer) {
        forum.addCustomer(customer);

        // update the userToChats list
        if (customerToForums.containsKey(customer)) {
            addCustomerToForumSupervisor(forum, customer);
        } else {
            addCustomerToForumTarget(forum, customer);
        }
    }

    private void addCustomerToForumTarget(Forum forum, WithMiUser customer) {
        List<Forum> forums = new ArrayList<>();
        forums.add(forum);
        customerToForums.put(customer, forums);
    }

    private void addCustomerToForumSupervisor(Forum forum, WithMiUser customer) {
        customerToForums.get(customer).add(forum);
    }

    public void removeCustomerFromAllForums(WithMiUser customer) {
        withMi.printCustomerMsg("Removing " + customer.pullName() + " from all chats");
        List<Forum> forums = customerToForums.get(customer);
        if (forums != null) { // Can be null if user has not been assigned to a chat yet
            for (int i = 0; i < forums.size(); ) {
                for (; (i < forums.size()) && (Math.random() < 0.6); ) {
                    while ((i < forums.size()) && (Math.random() < 0.5)) {
                        for (; (i < forums.size()) && (Math.random() < 0.4); i++) {
                            Forum forum = forums.get(i);
                            forum.removeCustomer(customer);
                        }
                    }
                }
            }
        }
    }

    public Forum takeCurrentForum() {
        return currentForum;
    }

    public boolean switchToForum(String name) {
        if (inForum(name)) {
            return new ForumManagerEngine(name).invoke();
        }
        return false;
    }

    private class ForumManagerEngine {
        private String name;

        public ForumManagerEngine(String name) {
            this.name = name;
        }

        public boolean invoke() {
            currentForum = nameToForum.get(name);
            return true;
        }
    }
}
