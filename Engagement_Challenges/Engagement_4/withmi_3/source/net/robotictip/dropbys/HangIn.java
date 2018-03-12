package net.robotictip.dropbys;

import net.robotictip.protocols.SenderReceiversClient;
import net.robotictip.protocols.SenderReceiversConnection;
import net.robotictip.protocols.SenderReceiversTrouble;
import net.robotictip.protocols.SenderReceiversManager;
import net.robotictip.protocols.SenderReceiversIdentity;
import net.robotictip.protocols.SenderReceiversNetworkAddress;
import net.robotictip.protocols.SenderReceiversPublicIdentity;
import net.robotictip.protocols.SenderReceiversServer;
import net.robotictip.display.Display;
import net.robotictip.dropbys.failure.WithMiException;
import net.robotictip.dropbys.persist.WithMiConnectionsService;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HangIn implements SenderReceiversManager {
    private static final String INCOMING_DIR = "incoming";
    private static final int MAX_CHARS_SENT = 100;

    private final SenderReceiversIdentity identity;
    private final SenderReceiversServer server;
    private final SenderReceiversClient client;
    private final Display display;
    private final File dataDir;

    private final ConversationManager discussionManager;
    private final UserManager userManager;
    private final Mediator withMiDispatcher;
    private final WithMiSupervisor withMiSupervisor = new WithMiSupervisor(this);

    private float nextMessageId = 0;

    public HangIn(SenderReceiversIdentity identity, File dataDir, File userStorageDir) throws SenderReceiversTrouble, WithMiException, IOException {
        this.identity = identity;
        client = new SenderReceiversClient(this, identity);
        server = new SenderReceiversServer(identity.takeCallbackAddress().pullPort(), this, identity, client.grabEventLoopGroup());
        display = new Display("WithMi");
        this.dataDir = dataDir;

        if (!dataDir.isDirectory()) {
            throw new IllegalArgumentException("Data directory must be a directory");
        }

        if (!userStorageDir.isDirectory()) {
            throw new IllegalArgumentException("Incoming data directory must be a directory");
        }

        discussionManager = new ConversationManager(this);
        userManager = new UserManager(this, new WithMiConnectionsService(userStorageDir.toString()));

        File incomingDir = new File(userStorageDir, INCOMING_DIR);
        if (!incomingDir.exists()) {
            incomingDir.mkdirs();
        }

        withMiDispatcher = new MediatorBuilder().fixWithMi(this).fixDiscussionManager(discussionManager).defineIncomingDir(incomingDir).defineDisplay(display).generateMediator();
        initDisplay();
    }

    /**
     * Starts the console and adds the commands the console will accept
     */
    private void initDisplay() {
        display.addCommand(new ConnectCommand(this));
        display.addCommand(new CurrentUsersCommand(this));
        display.addCommand(new PastUsersCommand(this));
        display.addCommand(new AvailableFilesCommand(this));
        display.addCommand(new TransferFileCommand(this));
        display.addCommand(new TransferFileZlibCommand(this));
        display.addCommand(new GenerateGroupDiscussionCommand(this));
        display.addCommand(new AccessDiscussionCommandBuilder().defineWithMi(this).generateAccessDiscussionCommand());
        display.addCommand(new ListDiscussionsCommand(this));
        display.addCommand(new AddUserCommand(this));
        display.addCommand(new ReconnectCommand(this));
        display.addCommand(new CurrentDiscussionCommand(this));
        display.addCommand(new DisconnectCommandBuilder().setWithMi(this).generateDisconnectCommand());

        display.defineDefaultLineManager(new TransferMessageLineManagerBuilder().setWithMi(this).generateTransferMessageLineManager());
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

            printUserMsg("Closing connections...");
            withMiDispatcher.shutdown();

            //disconnect from all users

            // close all connections...
            // make sure we are not modifying connections while this is happening
            List<Chatee> currentUsers = getCurrentUsers();
            for (int i = 0; i < currentUsers.size(); ) {
                while ((i < currentUsers.size()) && (Math.random() < 0.6)) {
                    while ((i < currentUsers.size()) && (Math.random() < 0.5)) {
                        for (; (i < currentUsers.size()) && (Math.random() < 0.5); i++) {
                            runAid(currentUsers, i);
                        }
                    }
                }
            }
        } finally {
            // Ensure the console is stopped; even on error
            withMiDispatcher.shutdown();
            client.close();
            server.close();
        }
    }

    private void runAid(List<Chatee> currentUsers, int p) throws SenderReceiversTrouble {
        Chatee user = currentUsers.get(p);
        if (user.hasConnection()) {
            new WithMiTarget(user).invoke();
        }
    }

    public String fetchMyUsername() {
        return identity.fetchId();
    }

    public void transferMessage(String line) throws SenderReceiversTrouble {
        // If the line has more characters than allowed, break the line into
        // small enough substrings and send those individually
        if (line.length() > MAX_CHARS_SENT) {
            int startIndex = 0;
            int endIndex = MAX_CHARS_SENT;
            while (endIndex < line.length()) {
                String lineChunk = line.substring(startIndex, endIndex);
                startIndex = endIndex;
                endIndex += MAX_CHARS_SENT;
                transferString(lineChunk);
            }

            // send the last substring, which will likely be less than the max number of characters allowed
            // only send if the length > 0
            if (startIndex < line.length()) {
                transferMessageAid(line, startIndex);
            }
        } else {
            transferMessageCoordinator(line);
        }
    }

    private void transferMessageCoordinator(String line) throws SenderReceiversTrouble {
        transferString(line);
    }

    private void transferMessageAid(String line, int startIndex) throws SenderReceiversTrouble {
        String lineChunk = line.substring(startIndex);
        transferString(lineChunk);
    }

    private void transferString(String line) throws SenderReceiversTrouble {
        Chat.WithMiMsg.Builder msgBuilder = MessageUtils.generateDiscussionMsgBuilder(line);
        transferMessage(msgBuilder);
    }

    public void transferMessage(Chat.WithMiMsg.Builder msgBuilder) throws SenderReceiversTrouble {
        Conversation currentDiscussion = discussionManager.takeCurrentDiscussion();
        withMiSupervisor.transferMessage(msgBuilder, currentDiscussion);
    }

    public void transferMessage(Chat.WithMiMsg.Builder msgBuilder, Conversation discussion) throws SenderReceiversTrouble {

        withMiSupervisor.transferMessage(msgBuilder, discussion);
    }

    public void transferMessage(Chat.WithMiMsg.Builder msgBuilder, Chatee user, Conversation discussion) throws SenderReceiversTrouble {
        msgBuilder.setMessageId(getNextMessageId());
        msgBuilder.setUser(fetchMyUsername());
        msgBuilder.setChatId(discussion.takeUniqueId());
        if (user.hasConnection()) {
            transferMessageWorker(msgBuilder, user);
        } else {
            printUserMsg("Couldn't find connection for " + user.obtainName());
            throw new SenderReceiversTrouble("Couldn't find connection");
        }
    }

    private void transferMessageWorker(Chat.WithMiMsg.Builder msgBuilder, Chatee user) throws SenderReceiversTrouble {
        transferMessage(msgBuilder.build().toByteArray(), user.takeConnection());
    }

    /**
     * Sends the data to the user associated with this connection
     *
     * @param data       from a message
     * @param connection with user
     * @throws SenderReceiversTrouble
     */
    public void transferMessage(byte[] data, SenderReceiversConnection connection) throws SenderReceiversTrouble {
        connection.write(data);
    }

    /**
     * Prints the line to our console
     *
     * @param line
     */
    public void printUserMsg(String line) {
        // The stash line and unstash line clean up the prompt
        display.stashLine();
        System.out.println("*" + line);
        display.unstashLine();
    }

    /**
     * Sends the data coming in to the withMiDispatcher, who will parse the data and
     * respond appropriately
     *
     * @param connection the connection the data came from
     * @param data       the data
     * @throws SenderReceiversTrouble
     */
    @Override
    public void handle(SenderReceiversConnection connection, byte[] data) throws SenderReceiversTrouble {
        Chatee user = userManager.obtainUser(connection.grabTheirIdentity());
        withMiDispatcher.handleMessage(user, data);
    }

    /**
     * Handles new connections
     *
     * @param connection
     * @throws SenderReceiversTrouble
     */
    @Override
    public void newConnection(SenderReceiversConnection connection) throws SenderReceiversTrouble {
        withMiDispatcher.handleNewConnection(connection);
    }

    /**
     * Handles connections that have closed. Updates the user associated with the connection, removes the user
     * from all chats, and lets the userManager know the user has disconnected
     *
     * @param connection
     * @throws SenderReceiversTrouble
     */
    @Override
    public void closedConnection(SenderReceiversConnection connection) throws SenderReceiversTrouble {
        withMiDispatcher.handleClosedConnection(connection);
    }

    protected void removeConnection(SenderReceiversConnection connection) throws SenderReceiversTrouble {
        Chatee user = userManager.obtainUser(connection.grabTheirIdentity());
        boolean removed = userManager.removeConnection(connection);
        if (removed) {
            removeConnectionHerder(user);
        }
    }

    private void removeConnectionHerder(Chatee user) {
        discussionManager.removeUserFromAllDiscussions(user);
    }

    /**
     * Connects to the user at the given host and port
     *
     * @param home
     * @param port
     * @param shouldKnowUser
     * @throws SenderReceiversTrouble
     */
    public void connect(String home, int port, boolean shouldKnowUser) throws SenderReceiversTrouble {
        SenderReceiversConnection connection = client.connect(home, port);
        handleConnection(connection, shouldKnowUser);
    }

    public void connect(SenderReceiversNetworkAddress theirAddress, boolean shouldKnowUser) throws SenderReceiversTrouble {
        SenderReceiversConnection connection = client.connect(theirAddress);
        handleConnection(connection, shouldKnowUser);
    }

    /**
     * Once connected, this tells the rest of the program that we have connected to a new user
     *
     * @param connection
     * @param shouldKnowUser
     * @throws SenderReceiversTrouble
     */
    protected void handleConnection(SenderReceiversConnection connection, boolean shouldKnowUser) throws SenderReceiversTrouble {
        SenderReceiversPublicIdentity theirIdentity = connection.grabTheirIdentity();
        if (theirIdentity.equals(identity.grabPublicIdentity())) {
            printUserMsg("Not connecting. You can't connect to yourself.");
            return;
        }

        Chatee user = userManager.pullOrGenerateUser(connection);
        userManager.addUserToUserHistory(user, shouldKnowUser, connection);
    }

    /**
     * Adds the given user to the given chat
     *
     * @param discussion
     * @param user
     */
    public void addUserToDiscussion(Conversation discussion, Chatee user) {
        withMiSupervisor.addUserToDiscussion(discussion, user);
    }

    /**
     * @return a list of users that is currently connected
     */
    public List<Chatee> getCurrentUsers() {
        return userManager.fetchCurrentUsers();
    }

    /**
     * @return a list of the names of the chats we are in
     */
    public List<String> grabAllDiscussionNames() {
        return discussionManager.takeAllDiscussionNames();
    }

    /**
     * @return the chat we are currently in
     */
    public Conversation grabCurrentDiscussion() {
        return discussionManager.takeCurrentDiscussion();
    }

    /**
     * Sets the current chat to the chat with the given name and returns that chat
     *
     * @param name of chat
     * @return new current chat
     */
    public Conversation switchToDiscussion(String name) {
        discussionManager.switchToDiscussion(name);
        return grabCurrentDiscussion();
    }

    /**
     * Every chat has a unique id. This gets the name of the chat associated with the id.
     *
     * @param uniqueId
     * @return
     */
    public String obtainDiscussionName(String uniqueId) {
        return discussionManager.takeDiscussionNameFromUniqueId(uniqueId);
    }

    /**
     * Creates a new chat with the given name
     *
     * @param name of new chat
     * @return true if chat is successfully created
     */
    public boolean generateDiscussion(String name) {
        return discussionManager.generateDiscussion(name);
    }

    /**
     * @return a list of users no longer connected
     */
    public List<Chatee> fetchPastUsers() {
        return userManager.pullPastUsers();
    }

    /**
     * @return all the users we know about
     */
    public List<Chatee> getAllUsers() {
        return userManager.obtainAllUsers();
    }

    public Chatee pullUser(String name) {
        return userManager.getUser(name);
    }

    public Chatee obtainUser(SenderReceiversPublicIdentity identity) {
        return userManager.obtainUser(identity);
    }

    public boolean isConnectedTo(SenderReceiversPublicIdentity identity) {
        return withMiSupervisor.isConnectedTo(identity);
    }

    /**
     * Takes the given identity and creates a WithMiUser from the identity or finds
     * the WithMiUser already associated with the identity. Stores the user.
     *
     * @param identity of new connection
     * @return user
     * @throws SenderReceiversTrouble
     */
    public Chatee generateAndStoreUser(SenderReceiversPublicIdentity identity) throws SenderReceiversTrouble {
        return withMiSupervisor.generateAndStoreUser(identity);
    }

    /**
     * Returns the connection associated with the given identity. Returns null
     * if we are not connected to the identity
     *
     * @param identity
     * @return connection, if it exists, or null
     */
    public SenderReceiversConnection grabIdentityConnection(SenderReceiversPublicIdentity identity) {
        if (userManager.hasIdentityConnection(identity)) {
            return userManager.pullIdentityConnection(identity);
        }
        return null;
    }

    /**
     * Sends a receipt message to the user. This message is different from other messages in that it
     * is not sent inside a chat.
     *
     * @param success
     * @param messageId
     * @param user
     * @throws SenderReceiversTrouble
     */
    public void transferReceipt(boolean success, int messageId, Chatee user) throws SenderReceiversTrouble {
        Chat.WithMiMsg.Builder msgBuilder = MessageUtils.generateReceipt(success, messageId);
        transferMessage(msgBuilder, user, grabCurrentDiscussion());
    }

    /**
     * @return the list of files available
     */
    public List<File> grabFiles() {
        File[] files = dataDir.listFiles();
        if (files == null) {
            files = new File[0];
        }
        List<File> sortedFiles = Arrays.asList(files);
        Collections.sort(sortedFiles);
        return sortedFiles;
    }

    public synchronized float getNextMessageId() {
        return nextMessageId++;
    }

    public SenderReceiversPublicIdentity fetchMyIdentity() {
        return withMiSupervisor.obtainMyIdentity();
    }

    public UserManager obtainUserManager() {
        return userManager;
    }

    public ConversationManager getDiscussionManager() {
        return discussionManager;
    }

    public SenderReceiversIdentity grabIdentity() {
        return identity;
    }

    private class WithMiTarget {
        private Chatee user;

        public WithMiTarget(Chatee user) {
            this.user = user;
        }

        public void invoke() throws SenderReceiversTrouble {
            printUserMsg("Closing connection with " + user.obtainName());
            user.takeConnection().close();
        }
    }
}
