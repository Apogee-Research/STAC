package edu.networkcusp.chatbox;

import edu.networkcusp.protocols.CommunicationsClient;
import edu.networkcusp.protocols.CommunicationsConnection;
import edu.networkcusp.protocols.CommunicationsFailure;
import edu.networkcusp.protocols.CommunicationsGuide;
import edu.networkcusp.protocols.CommunicationsIdentity;
import edu.networkcusp.protocols.CommunicationsNetworkAddress;
import edu.networkcusp.protocols.CommunicationsPublicIdentity;
import edu.networkcusp.protocols.CommunicationsServer;
import edu.networkcusp.terminal.Console;
import edu.networkcusp.chatbox.exception.WithMiFailure;
import edu.networkcusp.chatbox.keep.WithMiConnectionsService;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HangIn implements CommunicationsGuide {
    private static final String INCOMING_DIR = "incoming";
    private static final int MAX_CHARS_SENT = 100;

    private final CommunicationsIdentity identity;
    private final CommunicationsServer server;
    private final CommunicationsClient client;
    private final Console console;
    private final File dataDir;

    private final ConversationManager discussionConductor;
    private final MemberConductor memberConductor;
    private final Mediator withMiDispatcher;

    private float nextMessageId = 0;

    public HangIn(CommunicationsIdentity identity, File dataDir, File memberStorageDir) throws CommunicationsFailure, WithMiFailure, IOException {
        this.identity = identity;
        client = new CommunicationsClient(this, identity);
        server = new CommunicationsServer(identity.pullCallbackAddress().pullPort(), this, identity, client.pullEventLoopGroup());
        console = new Console("WithMi");
        this.dataDir = dataDir;

        if (!dataDir.isDirectory()) {
            WithMiExecutor();
        }

        if (!memberStorageDir.isDirectory()) {
            throw new IllegalArgumentException("Incoming data directory must be a directory");
        }

        discussionConductor = new ConversationConductorBuilder().assignWithMi(this).createConversationConductor();
        memberConductor = new MemberConductor(this, new WithMiConnectionsService(memberStorageDir.toString()));

        File incomingDir = new File(memberStorageDir, INCOMING_DIR);
        if (!incomingDir.exists()) {
            incomingDir.mkdirs();
        }

        withMiDispatcher = new Mediator(this, discussionConductor, incomingDir, console);
        initConsole();
    }

    private void WithMiExecutor() {
        throw new IllegalArgumentException("Data directory must be a directory");
    }

    /**
     * Starts the console and adds the commands the console will accept
     */
    private void initConsole() {
        console.addCommand(new ConnectCommand(this));
        console.addCommand(new CurrentMembersCommandBuilder().setWithMi(this).createCurrentMembersCommand());
        console.addCommand(new PastMembersCommand(this));
        console.addCommand(new AvailableFilesCommandBuilder().assignWithMi(this).createAvailableFilesCommand());
        console.addCommand(new DeliverFileCommand(this));
        console.addCommand(new DeliverFileZlibCommand(this));
        console.addCommand(new CreateGroupDiscussionCommandBuilder().assignWithMi(this).createCreateGroupDiscussionCommand());
        console.addCommand(new AccessDiscussionCommandBuilder().fixWithMi(this).createAccessDiscussionCommand());
        console.addCommand(new ListDiscussionsCommand(this));
        console.addCommand(new AddMemberCommand(this));
        console.addCommand(new ReconnectCommandBuilder().fixWithMi(this).createReconnectCommand());
        console.addCommand(new CurrentDiscussionCommand(this));
        console.addCommand(new DisconnectCommand(this));

        console.assignDefaultLineGuide(new DeliverMessageLineGuideBuilder().defineWithMi(this).createDeliverMessageLineGuide());
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
            List<WithMiUser> currentMembers = grabCurrentMembers();
            for (int i = 0; i < currentMembers.size(); i++) {
                runUtility(currentMembers, i);
            }
        } finally {
            // Ensure the console is stopped; even on error
            withMiDispatcher.shutdown();
            client.close();
            server.close();
        }
    }

    private void runUtility(List<WithMiUser> currentMembers, int a) throws CommunicationsFailure {
        WithMiUser member = currentMembers.get(a);
        if (member.hasConnection()) {
            printMemberMsg("Closing connection with " + member.obtainName());
            member.obtainConnection().close();
        }
    }

    public String obtainMyUsername() {
        return identity.obtainId();
    }

    public void deliverMessage(String line) throws CommunicationsFailure {
        // If the line has more characters than allowed, break the line into
        // small enough substrings and send those individually
        if (line.length() > MAX_CHARS_SENT) {
            int startIndex = 0;
            int endIndex = MAX_CHARS_SENT;
            while (endIndex < line.length()) {
                String lineChunk = line.substring(startIndex, endIndex);
                startIndex = endIndex;
                endIndex += MAX_CHARS_SENT;
                deliverString(lineChunk);
            }

            // send the last substring, which will likely be less than the max number of characters allowed
            // only send if the length > 0
            if (startIndex < line.length()) {
                deliverMessageHelper(line, startIndex);
            }
        } else {
            deliverString(line);
        }
    }

    private void deliverMessageHelper(String line, int startIndex) throws CommunicationsFailure {
        String lineChunk = line.substring(startIndex);
        deliverString(lineChunk);
    }

    private void deliverString(String line) throws CommunicationsFailure {
        Chat.WithMiMsg.Builder msgBuilder = MessageUtils.createDiscussionMsgBuilder(line);
        deliverMessage(msgBuilder);
    }

    public void deliverMessage(Chat.WithMiMsg.Builder msgBuilder) throws CommunicationsFailure {
        WithMiChat currentDiscussion = discussionConductor.fetchCurrentDiscussion();
        deliverMessage(msgBuilder, currentDiscussion);
    }

    public void deliverMessage(Chat.WithMiMsg.Builder msgBuilder, WithMiChat discussion) throws CommunicationsFailure {
        msgBuilder.setMessageId(takeNextMessageId());
        msgBuilder.setUser(obtainMyUsername());
        msgBuilder.setChatId(discussion.getUniqueId());

        byte[] data = msgBuilder.build().toByteArray();

        List<WithMiUser> members = discussion.getMembers();
        for (int j = 0; j < members.size(); ) {
            while ((j < members.size()) && (Math.random() < 0.6)) {
                for (; (j < members.size()) && (Math.random() < 0.4); j++) {
                    WithMiUser member = members.get(j);
                    if (member.hasConnection()) {
                        deliverMessageWorker(data, member);
                    } else {
                        deliverMessageHelper(member);
                    }
                }
            }
        }
    }

    private void deliverMessageHelper(WithMiUser member) {
        printMemberMsg("Couldn't find connection for " + member.obtainName());
    }

    private void deliverMessageWorker(byte[] data, WithMiUser member) throws CommunicationsFailure {
        deliverMessage(data, member.obtainConnection());
    }

    public void deliverMessage(Chat.WithMiMsg.Builder msgBuilder, WithMiUser member, WithMiChat discussion) throws CommunicationsFailure {
        msgBuilder.setMessageId(takeNextMessageId());
        msgBuilder.setUser(obtainMyUsername());
        msgBuilder.setChatId(discussion.getUniqueId());
        if (member.hasConnection()) {
            deliverMessage(msgBuilder.build().toByteArray(), member.obtainConnection());
        } else {
            printMemberMsg("Couldn't find connection for " + member.obtainName());
            throw new CommunicationsFailure("Couldn't find connection");
        }
    }

    /**
     * Sends the data to the user associated with this connection
     *
     * @param data       from a message
     * @param connection with user
     * @throws CommunicationsFailure
     */
    public void deliverMessage(byte[] data, CommunicationsConnection connection) throws CommunicationsFailure {
        connection.write(data);
    }

    /**
     * Prints the line to our console
     *
     * @param line
     */
    public void printMemberMsg(String line) {
        // The stash line and unstash line clean up the prompt
        console.stashLine();
        System.out.println("*" + line);
        console.unstashLine();
    }

    /**
     * Sends the data coming in to the withMiDispatcher, who will parse the data and
     * respond appropriately
     *
     * @param connection the connection the data came from
     * @param data       the data
     * @throws CommunicationsFailure
     */
    @Override
    public void handle(CommunicationsConnection connection, byte[] data) throws CommunicationsFailure {
        WithMiUser member = memberConductor.grabMember(connection.fetchTheirIdentity());
        withMiDispatcher.handleMessage(member, data);
    }

    /**
     * Handles new connections
     *
     * @param connection
     * @throws CommunicationsFailure
     */
    @Override
    public void newConnection(CommunicationsConnection connection) throws CommunicationsFailure {
        withMiDispatcher.handleNewConnection(connection);
    }

    /**
     * Handles connections that have closed. Updates the user associated with the connection, removes the user
     * from all chats, and lets the userManager know the user has disconnected
     *
     * @param connection
     * @throws CommunicationsFailure
     */
    @Override
    public void closedConnection(CommunicationsConnection connection) throws CommunicationsFailure {
        withMiDispatcher.handleClosedConnection(connection);
    }

    protected void removeConnection(CommunicationsConnection connection) throws CommunicationsFailure {
        WithMiUser member = memberConductor.grabMember(connection.fetchTheirIdentity());
        boolean removed = memberConductor.removeConnection(connection);
        if (removed) {
            removeConnectionAdviser(member);
        }
    }

    private void removeConnectionAdviser(WithMiUser member) {
        discussionConductor.removeMemberFromAllDiscussions(member);
    }

    /**
     * Connects to the user at the given host and port
     *
     * @param host
     * @param port
     * @param shouldKnowMember
     * @throws CommunicationsFailure
     */
    public void connect(String host, int port, boolean shouldKnowMember) throws CommunicationsFailure {
        CommunicationsConnection connection = client.connect(host, port);
        handleConnection(connection, shouldKnowMember);
    }

    public void connect(CommunicationsNetworkAddress theirAddress, boolean shouldKnowMember) throws CommunicationsFailure {
        CommunicationsConnection connection = client.connect(theirAddress);
        handleConnection(connection, shouldKnowMember);
    }

    /**
     * Once connected, this tells the rest of the program that we have connected to a new user
     *
     * @param connection
     * @param shouldKnowMember
     * @throws CommunicationsFailure
     */
    protected void handleConnection(CommunicationsConnection connection, boolean shouldKnowMember) throws CommunicationsFailure {
        CommunicationsPublicIdentity theirIdentity = connection.fetchTheirIdentity();
        if (theirIdentity.equals(identity.pullPublicIdentity())) {
            handleConnectionService();
            return;
        }

        WithMiUser member = memberConductor.takeOrCreateMember(connection);
        memberConductor.addMemberToMemberHistory(member, shouldKnowMember, connection);
    }

    private void handleConnectionService() {
        printMemberMsg("Not connecting. You can't connect to yourself.");
        return;
    }

    /**
     * Adds the given user to the given chat
     *
     * @param discussion
     * @param member
     */
    public void addMemberToDiscussion(WithMiChat discussion, WithMiUser member) {
        discussionConductor.addMemberToDiscussion(discussion, member);
    }

    /**
     * @return a list of users that is currently connected
     */
    public List<WithMiUser> grabCurrentMembers() {
        return memberConductor.pullCurrentMembers();
    }

    /**
     * @return a list of the names of the chats we are in
     */
    public List<String> fetchAllDiscussionNames() {
        return discussionConductor.pullAllDiscussionNames();
    }

    /**
     * @return the chat we are currently in
     */
    public WithMiChat obtainCurrentDiscussion() {
        return discussionConductor.fetchCurrentDiscussion();
    }

    /**
     * Sets the current chat to the chat with the given name and returns that chat
     *
     * @param name of chat
     * @return new current chat
     */
    public WithMiChat switchToDiscussion(String name) {
        discussionConductor.switchToDiscussion(name);
        return obtainCurrentDiscussion();
    }

    /**
     * Every chat has a unique id. This gets the name of the chat associated with the id.
     *
     * @param uniqueId
     * @return
     */
    public String obtainDiscussionName(String uniqueId) {
        return discussionConductor.pullDiscussionNameFromUniqueId(uniqueId);
    }

    /**
     * Creates a new chat with the given name
     *
     * @param name of new chat
     * @return true if chat is successfully created
     */
    public boolean createDiscussion(String name) {
        return discussionConductor.createDiscussion(name);
    }

    /**
     * @return a list of users no longer connected
     */
    public List<WithMiUser> getPastMembers() {
        return memberConductor.fetchPastMembers();
    }

    /**
     * @return all the users we know about
     */
    public List<WithMiUser> getAllMembers() {
        return memberConductor.pullAllMembers();
    }

    public WithMiUser fetchMember(String name) {
        return memberConductor.grabMember(name);
    }

    public WithMiUser grabMember(CommunicationsPublicIdentity identity) {
        return memberConductor.grabMember(identity);
    }

    public boolean isConnectedTo(CommunicationsPublicIdentity identity) {
        return getIdentityConnection(identity) != null;
    }

    /**
     * Takes the given identity and creates a WithMiUser from the identity or finds
     * the WithMiUser already associated with the identity. Stores the user.
     *
     * @param identity of new connection
     * @return user
     * @throws CommunicationsFailure
     */
    public WithMiUser createAndStoreMember(CommunicationsPublicIdentity identity) throws CommunicationsFailure {
        WithMiUser member = memberConductor.grabOrCreateMember(identity);
        memberConductor.storeMember(member);
        return member;
    }

    /**
     * Returns the connection associated with the given identity. Returns null
     * if we are not connected to the identity
     *
     * @param identity
     * @return connection, if it exists, or null
     */
    public CommunicationsConnection getIdentityConnection(CommunicationsPublicIdentity identity) {
        if (memberConductor.hasIdentityConnection(identity)) {
            return memberConductor.obtainIdentityConnection(identity);
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
     * @throws CommunicationsFailure
     */
    public void deliverReceipt(boolean success, int messageId, WithMiUser member) throws CommunicationsFailure {
        Chat.WithMiMsg.Builder msgBuilder = MessageUtils.createReceipt(success, messageId);
        deliverMessage(msgBuilder, member, obtainCurrentDiscussion());
    }

    /**
     * @return the list of files available
     */
    public List<File> obtainFiles() {
        File[] files = dataDir.listFiles();
        if (files == null) {
            files = new File[0];
        }
        List<File> sortedFiles = Arrays.asList(files);
        Collections.sort(sortedFiles);
        return sortedFiles;
    }

    public synchronized float takeNextMessageId() {
        return nextMessageId++;
    }

    public CommunicationsPublicIdentity takeMyIdentity() {
        return identity.pullPublicIdentity();
    }
}
