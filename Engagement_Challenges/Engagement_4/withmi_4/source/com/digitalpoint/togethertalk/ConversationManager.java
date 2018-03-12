package com.digitalpoint.togethertalk;

import com.digitalpoint.dialogs.Comms;
import com.digitalpoint.dialogs.SenderReceiversException;
import com.digitalpoint.dialogs.SenderReceiversPublicIdentity;
import com.digitalpoint.dialogs.SerializerUtil;

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
    private final Map<String, Forum> nameToConference = new HashMap<>();

    /**
     * maps unique id to chat name
     */
    private final Map<String, String> idToName = new HashMap<>();

    private final Map<Participant, List<Forum>> memberToConferences = new HashMap<>();

    private final HangIn withMi;

    /**
     * the current chat
     */
    private Forum currentConference;

    public ConversationManager(HangIn withMi) {
        this.withMi = withMi;
        makeConference(DEFAULT_CHAT_NAME);
        currentConference = obtainConference(DEFAULT_CHAT_NAME);
    }

    /**
     * Returns a chat associated with the given name
     *
     * @param name of chat
     * @return chat
     */
    public Forum obtainConference(String name) {
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
            withMi.printMemberMsg("A chat with this name already exists");
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

        Forum groupConference = new Forum(withMi, conferenceName, uniqueId);
        nameToConference.put(conferenceName, groupConference);
        idToName.put(groupConference.pullUniqueId(), conferenceName);
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
    public String fetchConferenceNameFromUniqueId(String uniqueId) {
        return idToName.get(uniqueId);
    }

    /**
     * @return names of all the chats we're in
     */
    public List<String> grabAllConferenceNames() {
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
        Forum conference = obtainConference(fetchConferenceNameFromUniqueId(uniqueId));
        String text = conferenceMsg.getTextMsg();
        String readableMsg = sender + ": " + text;
        if (conference.equals(currentConference)) {
            withMi.printMemberMsg(readableMsg);
        } else {
            conference.storeUnreadMessage(readableMsg);
        }
    }

    /**
     * Receives a chat state message, which indicates that the given chat has changed in some way. This message
     * received when a user has been added to a chat we are already in, or we have been added to a previously unknown chat.
     *
     * @param msg
     * @param conferenceId   of the new chat
     * @param conferenceName suggested by the user adding us to the chat
     * @throws SenderReceiversException
     */
   public void handleConferenceStateMessage(Chat.ChatStateMsg msg, String conferenceId, String conferenceName) throws SenderReceiversException {
       // if we don't know about this chat, it means we are being added to a new chat
       // create this chat
       if (fetchConferenceNameFromUniqueId(conferenceId) == null) {
           makeConference(conferenceName, conferenceId);
           withMi.printMemberMsg("Added new chat " + fetchConferenceNameFromUniqueId(conferenceId));
       }

       Forum conference = obtainConference(fetchConferenceNameFromUniqueId(conferenceId));

       // get the list of user identities in this chat
       List<SenderReceiversPublicIdentity> membersInConference = new ArrayList<>();
       List<Comms.Identity> publicIdList = msg.getPublicIdList();
       for (int j = 0; j < publicIdList.size(); j++) {
           handleConferenceStateMessageCoach(membersInConference, publicIdList, j);
       }

       // only connect to the identities before ours in the list
       boolean beforeMe = true;
       for (int j = 0; j < membersInConference.size(); j++) {
           SenderReceiversPublicIdentity identity = membersInConference.get(j);
           if (identity.equals(withMi.getMyIdentity())) {
               beforeMe = false;
               continue;
           }

           if (beforeMe && !withMi.isConnectedTo(identity)) {
               // if we don't know about this identity and it's before us in the list,
               //  connect
               handleConferenceStateMessageEngine(identity);
           }


           // get the WithMiUser with this identity or create one without a connection
           Participant member;
           if (withMi.isConnectedTo(identity)) {
               member = withMi.fetchMember(identity);
           } else {
               member = withMi.makeAndStoreMember(identity);
           }

           // if it isn't in the chat, add it
           if (!conference.containsMember(member)) {
               handleConferenceStateMessageCoordinator(conference, member);
           }
       }
   }

    private void handleConferenceStateMessageCoordinator(Forum conference, Participant member) {
        new ConferenceManagerExecutor(conference, member).invoke();
    }

    private void handleConferenceStateMessageEngine(SenderReceiversPublicIdentity identity) throws SenderReceiversException {
        withMi.connect(identity.fetchCallbackAddress(), false);
    }

    private void handleConferenceStateMessageCoach(List<SenderReceiversPublicIdentity> membersInConference, List<Comms.Identity> publicIdList, int a) {
        Comms.Identity publicId = publicIdList.get(a);
        SenderReceiversPublicIdentity identity = SerializerUtil.deserializeIdentity(publicId);
        // Users with the null identity shouldn't be added to this chat
        if (!identity.equals(Forum.NULL_IDENTITY)) {
            membersInConference.add(identity);
        }
    }

    public void addMemberToConference(Forum conference, Participant member) {
        conference.addMember(member);

        // update the userToChats list
        if (memberToConferences.containsKey(member)) {
            memberToConferences.get(member).add(conference);
        } else {
            addMemberToConferenceAdviser(conference, member);
        }
    }

    private void addMemberToConferenceAdviser(Forum conference, Participant member) {
        List<Forum> conferences = new ArrayList<>();
        conferences.add(conference);
        memberToConferences.put(member, conferences);
    }

    public void removeMemberFromAllConferences(Participant member) {
        withMi.printMemberMsg("Removing " + member.grabName() + " from all chats");
        List<Forum> conferences = memberToConferences.get(member);
        if (conferences != null) { // Can be null if user has not been assigned to a chat yet
            for (int q = 0; q < conferences.size(); q++) {
                removeMemberFromAllConferencesHome(member, conferences, q);
            }
        }
    }

    private void removeMemberFromAllConferencesHome(Participant member, List<Forum> conferences, int p) {
        Forum conference = conferences.get(p);
        conference.removeMember(member);
    }

    public Forum takeCurrentConference() {
        return currentConference;
    }

    public boolean switchToConference(String name) {
        if (inConference(name)) {
            return switchToConferenceGateKeeper(name);
        }
        return false;
    }

    private boolean switchToConferenceGateKeeper(String name) {
        currentConference = nameToConference.get(name);
        return true;
    }

    private class ConferenceManagerExecutor {
        private Forum conference;
        private Participant member;

        public ConferenceManagerExecutor(Forum conference, Participant member) {
            this.conference = conference;
            this.member = member;
        }

        public void invoke() {
            withMi.printMemberMsg("Adding " + member.grabName() + " to " + conference.pullName());
            addMemberToConference(conference, member);
        }
    }
}
