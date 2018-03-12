package com.digitalpoint.togethertalk;

import com.digitalpoint.dialogs.SenderReceiversClient;
import com.digitalpoint.dialogs.SenderReceiversConnection;
import com.digitalpoint.dialogs.SenderReceiversException;
import com.digitalpoint.dialogs.SenderReceiversCoach;
import com.digitalpoint.dialogs.SenderReceiversIdentity;
import com.digitalpoint.dialogs.SenderReceiversNetworkAddress;
import com.digitalpoint.dialogs.SenderReceiversPublicIdentity;
import com.digitalpoint.dialogs.SenderReceiversServer;
import com.digitalpoint.terminal.Console;
import com.digitalpoint.togethertalk.failure.WithMiException;
import com.digitalpoint.togethertalk.keep.WithMiConnectionsService;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HangIn implements SenderReceiversCoach {
    private static final String INCOMING_DIR = "incoming";
    private static final int MAX_CHARS_SENT = 100;

    private final SenderReceiversIdentity identity;
    private final SenderReceiversServer server;
    private final SenderReceiversClient client;
    private final Console command;
    private final File dataDir;

    private final ConversationManager conferenceManager;
    private final MemberManager memberManager;
    private final Dispatcher withMiDispatcher;

    private float nextMessageId = 0;

    public HangIn(SenderReceiversIdentity identity, File dataDir, File memberStorageDir) throws SenderReceiversException, WithMiException, IOException {
        this.identity = identity;
        client = new SenderReceiversClient(this, identity);
        server = new SenderReceiversServer(identity.fetchCallbackAddress().pullPort(), this, identity, client.obtainEventLoopGroup());
        command = new Console("WithMi");
        this.dataDir = dataDir;

        if (!dataDir.isDirectory()) {
            throw new IllegalArgumentException("Data directory must be a directory");
        }

        if (!memberStorageDir.isDirectory()) {
            throw new IllegalArgumentException("Incoming data directory must be a directory");
        }

        conferenceManager = new ConversationManager(this);
        memberManager = new MemberManager(this, new WithMiConnectionsService(memberStorageDir.toString()));

        File incomingDir = new File(memberStorageDir, INCOMING_DIR);
        if (!incomingDir.exists()) {
            incomingDir.mkdirs();
        }

        withMiDispatcher = new DispatcherBuilder().assignWithMi(this).defineConferenceManager(conferenceManager).fixIncomingDir(incomingDir).defineCommand(command).makeDispatcher();
        initCommand();
    }

    /**
     * Starts the console and adds the commands the console will accept
     */
    private void initCommand() {
        command.addCommand(new ConnectCommand(this));
        command.addCommand(new CurrentMembersCommand(this));
        command.addCommand(new PastMembersCommand(this));
        command.addCommand(new AvailableFilesCommand(this));
        command.addCommand(new SendFileCommand(this));
        command.addCommand(new SendFileZlibCommand(this));
        command.addCommand(new MakeGroupConferenceCommandBuilder().defineWithMi(this).makeMakeGroupConferenceCommand());
        command.addCommand(new JoinConferenceCommand(this));
        command.addCommand(new ListConferencesCommand(this));
        command.addCommand(new AddMemberCommandBuilder().setWithMi(this).makeAddMemberCommand());
        command.addCommand(new ReconnectCommandBuilder().fixWithMi(this).makeReconnectCommand());
        command.addCommand(new CurrentConferenceCommandBuilder().defineWithMi(this).makeCurrentConferenceCommand());
        command.addCommand(new DisconnectCommand(this));

        command.setDefaultLineCoach(new SendMessageLineCoach(this));
    }

    /**
     * Runs the chat program until the user asks to exit
     *
     * @throws Throwable if there is a problem executing commands
     */
    public void run() throws Throwable {
        server.serve();

        try {
            // While waiting for the console to conclude,
            // process the results of all background dispatches in the
            // main thread so the user can be notified of any issues.
            withMiDispatcher.run();

            printMemberMsg("Closing connections...");
            withMiDispatcher.shutdown();

            //disconnect from all users

            // close all connections...
            // make sure we are not modifying connections while this is happening
            List<Participant> currentMembers = getCurrentMembers();
            for (int i = 0; i < currentMembers.size(); i++) {
                Participant member = currentMembers.get(i);
                if (member.hasConnection()) {
                    runTarget(member);
                }
            }
        } finally {
            // Ensure the console is stopped; even on error
            withMiDispatcher.shutdown();
            client.close();
            server.close();
        }
    }

    private void runTarget(Participant member) throws SenderReceiversException {
        printMemberMsg("Closing connection with " + member.grabName());
        member.takeConnection().close();
    }

    public String takeMyUsername() {
        return identity.fetchId();
    }

    public void sendMessage(String line) throws SenderReceiversException {
        // If the line has more characters than allowed, break the line into
        // small enough substrings and send those individually
        if (line.length() > MAX_CHARS_SENT) {
            int startIndex = 0;
            int endIndex = MAX_CHARS_SENT;
            while (endIndex < line.length()) {
                String lineChunk = line.substring(startIndex, endIndex);
                startIndex = endIndex;
                endIndex += MAX_CHARS_SENT;
                sendString(lineChunk);
            }

            // send the last substring, which will likely be less than the max number of characters allowed
            // only send if the length > 0
            if (startIndex < line.length()) {
                sendMessageGuide(line, startIndex);
            }
        } else {
            sendString(line);
        }
    }

    private void sendMessageGuide(String line, int startIndex) throws SenderReceiversException {
        String lineChunk = line.substring(startIndex);
        sendString(lineChunk);
    }

    private void sendString(String line) throws SenderReceiversException {
        Chat.WithMiMsg.Builder msgBuilder = MessageUtils.makeConferenceMsgBuilder(line);
        sendMessage(msgBuilder);
    }

    public void sendMessage(Chat.WithMiMsg.Builder msgBuilder) throws SenderReceiversException {
        Forum currentConference = conferenceManager.takeCurrentConference();
        sendMessage(msgBuilder, currentConference);
    }

    public void sendMessage(Chat.WithMiMsg.Builder msgBuilder, Forum conference) throws SenderReceiversException {
        msgBuilder.setMessageId(obtainNextMessageId());
        msgBuilder.setUser(takeMyUsername());
        msgBuilder.setChatId(conference.pullUniqueId());

        byte[] data = msgBuilder.build().toByteArray();

        List<Participant> members = conference.pullMembers();
        for (int p = 0; p < members.size(); p++) {
            sendMessageAid(data, members, p);
        }
    }

    private void sendMessageAid(byte[] data, List<Participant> members, int b) throws SenderReceiversException {
        Participant member = members.get(b);
        if (member.hasConnection()) {
            sendMessage(data, member.takeConnection());
        } else {
            printMemberMsg("Couldn't find connection for " + member.grabName());
        }
    }

    public void sendMessage(Chat.WithMiMsg.Builder msgBuilder, Participant member, Forum conference) throws SenderReceiversException {
        msgBuilder.setMessageId(obtainNextMessageId());
        msgBuilder.setUser(takeMyUsername());
        msgBuilder.setChatId(conference.pullUniqueId());
        if (member.hasConnection()) {
            sendMessage(msgBuilder.build().toByteArray(), member.takeConnection());
        } else {
            printMemberMsg("Couldn't find connection for " + member.grabName());
            throw new SenderReceiversException("Couldn't find connection");
        }
    }

    /**
     * Sends the data to the user associated with this connection
     *
     * @param data       from a message
     * @param connection with user
     * @throws SenderReceiversException
     */
    public void sendMessage(byte[] data, SenderReceiversConnection connection) throws SenderReceiversException {
        connection.write(data);
    }

    /**
     * Prints the line to our console
     *
     * @param line
     */
    public void printMemberMsg(String line) {
        // The stash line and unstash line clean up the prompt
        command.stashLine();
        System.out.println("*" + line);
        command.unstashLine();
    }

    /**
     * Sends the data coming in to the withMiDispatcher, who will parse the data and
     * respond appropriately
     *
     * @param connection the connection the data came from
     * @param data       the data
     * @throws SenderReceiversException
     */
    @Override
    public void handle(SenderReceiversConnection connection, byte[] data) throws SenderReceiversException {
        Participant member = memberManager.getMember(connection.getTheirIdentity());
        withMiDispatcher.handleMessage(member, data);
    }

    /**
     * Handles new connections
     *
     * @param connection
     * @throws SenderReceiversException
     */
    @Override
    public void newConnection(SenderReceiversConnection connection) throws SenderReceiversException {
        withMiDispatcher.handleNewConnection(connection);
    }

    /**
     * Handles connections that have closed. Updates the user associated with the connection, removes the user
     * from all chats, and lets the userManager know the user has disconnected
     *
     * @param connection
     * @throws SenderReceiversException
     */
    @Override
    public void closedConnection(SenderReceiversConnection connection) throws SenderReceiversException {
        withMiDispatcher.handleClosedConnection(connection);
    }

    protected void removeConnection(SenderReceiversConnection connection) throws SenderReceiversException {
        Participant member = memberManager.getMember(connection.getTheirIdentity());
        boolean removed = memberManager.removeConnection(connection);
        if (removed) {
            conferenceManager.removeMemberFromAllConferences(member);
        }
    }

    /**
     * Connects to the user at the given host and port
     *
     * @param place
     * @param port
     * @param shouldKnowMember
     * @throws SenderReceiversException
     */
    public void connect(String place, int port, boolean shouldKnowMember) throws SenderReceiversException {
        SenderReceiversConnection connection = client.connect(place, port);
        handleConnection(connection, shouldKnowMember);
    }

    public void connect(SenderReceiversNetworkAddress theirAddress, boolean shouldKnowMember) throws SenderReceiversException {
        SenderReceiversConnection connection = client.connect(theirAddress);
        handleConnection(connection, shouldKnowMember);
    }

    /**
     * Once connected, this tells the rest of the program that we have connected to a new user
     *
     * @param connection
     * @param shouldKnowMember
     * @throws SenderReceiversException
     */
    protected void handleConnection(SenderReceiversConnection connection, boolean shouldKnowMember) throws SenderReceiversException {
        SenderReceiversPublicIdentity theirIdentity = connection.getTheirIdentity();
        if (theirIdentity.equals(identity.obtainPublicIdentity())) {
            handleConnectionAid();
            return;
        }

        Participant member = memberManager.getOrMakeMember(connection);
        memberManager.addMemberToMemberHistory(member, shouldKnowMember, connection);
    }

    private void handleConnectionAid() {
        printMemberMsg("Not connecting. You can't connect to yourself.");
        return;
    }

    /**
     * Adds the given user to the given chat
     *
     * @param conference
     * @param member
     */
    public void addMemberToConference(Forum conference, Participant member) {
        conferenceManager.addMemberToConference(conference, member);
    }

    /**
     * @return a list of users that is currently connected
     */
    public List<Participant> getCurrentMembers() {
        return memberManager.grabCurrentMembers();
    }

    /**
     * @return a list of the names of the chats we are in
     */
    public List<String> fetchAllConferenceNames() {
        return conferenceManager.grabAllConferenceNames();
    }

    /**
     * @return the chat we are currently in
     */
    public Forum obtainCurrentConference() {
        return conferenceManager.takeCurrentConference();
    }

    /**
     * Sets the current chat to the chat with the given name and returns that chat
     *
     * @param name of chat
     * @return new current chat
     */
    public Forum switchToConference(String name) {
        conferenceManager.switchToConference(name);
        return obtainCurrentConference();
    }

    /**
     * Every chat has a unique id. This gets the name of the chat associated with the id.
     *
     * @param uniqueId
     * @return
     */
    public String fetchConferenceName(String uniqueId) {
        return conferenceManager.fetchConferenceNameFromUniqueId(uniqueId);
    }

    /**
     * Creates a new chat with the given name
     *
     * @param name of new chat
     * @return true if chat is successfully created
     */
    public boolean makeConference(String name) {
        return conferenceManager.makeConference(name);
    }

    /**
     * @return a list of users no longer connected
     */
    public List<Participant> pullPastMembers() {
        return memberManager.getPastMembers();
    }

    /**
     * @return all the users we know about
     */
    public List<Participant> fetchAllMembers() {
        return memberManager.grabAllMembers();
    }

    public Participant getMember(String name) {
        return memberManager.grabMember(name);
    }

    public Participant fetchMember(SenderReceiversPublicIdentity identity) {
        return memberManager.getMember(identity);
    }

    public boolean isConnectedTo(SenderReceiversPublicIdentity identity) {
        return obtainIdentityConnection(identity) != null;
    }

    /**
     * Takes the given identity and creates a WithMiUser from the identity or finds
     * the WithMiUser already associated with the identity. Stores the user.
     *
     * @param identity of new connection
     * @return user
     * @throws SenderReceiversException
     */
    public Participant makeAndStoreMember(SenderReceiversPublicIdentity identity) throws SenderReceiversException {
        Participant member = memberManager.pullOrMakeMember(identity);
        memberManager.storeMember(member);
        return member;
    }

    /**
     * Returns the connection associated with the given identity. Returns null
     * if we are not connected to the identity
     *
     * @param identity
     * @return connection, if it exists, or null
     */
    public SenderReceiversConnection obtainIdentityConnection(SenderReceiversPublicIdentity identity) {
        if (memberManager.hasIdentityConnection(identity)) {
            return memberManager.fetchIdentityConnection(identity);
        }
        return null;
    }

    /**
     * Sends a receipt message to the user. This message is different from other messages in that it
     * is not sent inside a chat.
     *
     * @param success
     * @param messageId
     * @param member
     * @throws SenderReceiversException
     */
    public void sendReceipt(boolean success, int messageId, Participant member) throws SenderReceiversException {
        Chat.WithMiMsg.Builder msgBuilder = MessageUtils.makeReceipt(success, messageId);
        sendMessage(msgBuilder, member, obtainCurrentConference());
    }

    /**
     * @return the list of files available
     */
    public List<File> takeFiles() {
        File[] files = dataDir.listFiles();
        if (files == null) {
            files = new File[0];
        }
        List<File> sortedFiles = Arrays.asList(files);
        Collections.sort(sortedFiles);
        return sortedFiles;
    }

    public synchronized float obtainNextMessageId() {
        return nextMessageId++;
    }

    public SenderReceiversPublicIdentity getMyIdentity() {
        return identity.obtainPublicIdentity();
    }
}
