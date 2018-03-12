package com.digitalpoint.togethertalk;

import com.digitalpoint.dialogs.SenderReceiversConnection;
import com.digitalpoint.dialogs.SenderReceiversException;
import com.digitalpoint.dialogs.SenderReceiversPublicIdentity;
import com.digitalpoint.togethertalk.keep.WithMiConnectionsService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemberManager {
    private final HangIn withMi;

    /** maps user name to the WithMiUser */
    private final Map<String, Participant> nameToMember = new HashMap<>();

    /** maps the identity to a WithMiUser */
    private final Map<SenderReceiversPublicIdentity, Participant> idToMember = new HashMap<>();

    /** a list of users we are no longer connected to */
    private final List<Participant> pastMembers = new ArrayList<>();

    /** Handles previous connections */
    private final WithMiConnectionsService connectionsService;

    public MemberManager(HangIn withMi, WithMiConnectionsService connectionsService) throws SenderReceiversException {
        this.withMi = withMi;
        this.connectionsService = connectionsService;
        nameToMember.putAll(connectionsService.readInPreviousConnections());
        pastMembers.addAll(nameToMember.values());
        idToMember.putAll(makeMapFromIdentitiesToMembers(pastMembers));
    }

    /**
     * Creates a WithMiUser for the given connection, unless the identity
     * associated with the connection already belongs to a user.
     * If the identity already has a user that user is returned.
     * @return
     */
    private Participant getOrMakeMember(SenderReceiversPublicIdentity identity, SenderReceiversConnection connection) throws SenderReceiversException {

        if (idToMember.containsKey(identity) ) {
            Participant oldMember = idToMember.get(identity);
            oldMember.defineConnection(connection);

            // remove user from past users if they are there
            if (pastMembers.contains(oldMember)) {
                grabOrMakeMemberSupervisor(oldMember);
            }

            return oldMember;
        }

        String name = identity.getId();
        if (nameToMember.containsKey(name)) {
            // already have a user with this name, we need to give them some different identifier...
            int suffixId = 1;
            String newName = name + "_" + Integer.toHexString(suffixId);
            while (nameToMember.containsKey(newName)) {
                suffixId++;
                newName = name + "_" + Integer.toHexString(suffixId);
            }
            name = newName;
        }
        Participant member;
        if (connection != null) {
            member = new Participant(name, connection);
        } else {
            member = new Participant(name, identity);
        }

        return member;
    }

    private void grabOrMakeMemberSupervisor(Participant oldMember) {
        pastMembers.remove(oldMember);
    }

    public Participant pullOrMakeMember(SenderReceiversPublicIdentity identity) throws SenderReceiversException {
        return getOrMakeMember(identity, null);
    }

    public Participant getOrMakeMember(SenderReceiversConnection connection) throws SenderReceiversException {
        return getOrMakeMember(connection.getTheirIdentity(), connection);
    }

    public boolean removeConnection(SenderReceiversConnection connection) throws SenderReceiversException {
        SenderReceiversPublicIdentity identity = connection.getTheirIdentity();
        Participant member = idToMember.get(identity);
        if (member.takeConnection() == null) {
            // Already removed; may happen during shutdown
            return false;
        }
        if (member.takeConnection().equals(connection)) {
            member.removeConnection();
            pastMembers.add(member);
            return true;
        }
        throw new SenderReceiversException("Connection is not associated with correct user");
    }

    /**
     * @param name of user
     * @return true if the manager knows this name
     */
    public boolean knowsMember(String name) {
        return nameToMember.containsKey(name);
    }

    /**
     * Tells the connections service to store the user
     * @param member to store
     * @throws IOException
     */
    public void storeMember(Participant member) throws SenderReceiversException {
        String name = member.grabName();
        SenderReceiversPublicIdentity identity = member.getIdentity();
        nameToMember.put(name, member);
        idToMember.put(identity, member);
        ArrayList<Participant> storedMembers = new ArrayList<>(connectionsService.addMemberToFile(member));

        // check that the storedUsers matches known users
        if (!storedMembers.containsAll(nameToMember.values())) {
            storeMemberHome();
        }
    }

    private void storeMemberHome() throws SenderReceiversException {
        throw new SenderReceiversException("Stored users and known users are not the same");
    }

    /**
     * Adds the user to known users and creates the message we print ourselves once we've connected to this user.
     * The message changes depending on whether or not we have previously connected to the user.
     * @param member
     * @param shouldBeKnownMember
     * @param connection
     * @throws SenderReceiversException
     */
    public void addMemberToMemberHistory(Participant member, boolean shouldBeKnownMember, SenderReceiversConnection connection) throws SenderReceiversException {

        boolean previouslyConnected = knowsMember(member.grabName());
        StringBuilder stringBuilder = new StringBuilder();

        // if we should know the user and we don't, add that to the message
        // this will likely only happen when using the reconnect command
        if (shouldBeKnownMember && !previouslyConnected) {
            stringBuilder.append("WARNING: " + member.grabName() + " has a different identity. This may not be the same user");
        }

        if (!previouslyConnected) {
            
            // if we haven't previously connected, add this new user to our file
            new MemberManagerExecutor(member, stringBuilder).invoke();
        } else {
            addMemberToMemberHistoryAdviser(stringBuilder);
        }

        stringBuilder.append(member.grabName());
        if (member.hasCallbackAddress()) {
            stringBuilder.append(". callback on: " + member.obtainCallbackAddress());
        }
        withMi.sendReceipt(true, 0, member);

        

        withMi.printMemberMsg(stringBuilder.toString());

    }

    private void addMemberToMemberHistoryAdviser(StringBuilder stringBuilder) {
        stringBuilder.append("Reconnected to ");
    }

    public Participant grabMember(String name) {
        if (nameToMember.containsKey(name)) {
            return nameToMember.get(name);
        }
        return null;
    }

    public Participant getMember(SenderReceiversPublicIdentity identity) {
        if (idToMember.containsKey(identity)) {
            return idToMember.get(identity);
        }
        return null;
    }

    public SenderReceiversConnection fetchIdentityConnection(SenderReceiversPublicIdentity identity) {
        Participant member = idToMember.get(identity);
        if (member.hasConnection()) {
            return member.takeConnection();
        }
        return null;
    }

    public boolean hasIdentityConnection(SenderReceiversPublicIdentity identity) {
        if (!idToMember.containsKey(identity)) {
            return false;
        }
        Participant member = idToMember.get(identity);
        return member.hasConnection();
    }

    public List<Participant> grabAllMembers() {
        return new ArrayList<>(idToMember.values());
    }

    public List<Participant> getPastMembers() {
        return new ArrayList<>(pastMembers);
    }

    public List<Participant> grabCurrentMembers() {
        List<Participant> members = new ArrayList<>(nameToMember.values());
        members.removeAll(pastMembers);
        return members;
    }

    /**
     * Creates a map from CommsPublicIdentities to WithMiUsers.
     * @param members to be mapped
     * @return map
     */
    private Map<SenderReceiversPublicIdentity, Participant> makeMapFromIdentitiesToMembers(List<Participant> members) {
        Map<SenderReceiversPublicIdentity, Participant> identityToMemberMap = new HashMap<>();
        for (int a = 0; a < members.size(); a++) {
            Participant member = members.get(a);
            identityToMemberMap.put(member.getIdentity(), member);
        }
        return identityToMemberMap;
    }

    private class MemberManagerExecutor {
        private Participant member;
        private StringBuilder stringBuilder;

        public MemberManagerExecutor(Participant member, StringBuilder stringBuilder) {
            this.member = member;
            this.stringBuilder = stringBuilder;
        }

        public void invoke() throws SenderReceiversException {
            storeMember(member);

            stringBuilder.append("Connected to new user ");
        }
    }
}