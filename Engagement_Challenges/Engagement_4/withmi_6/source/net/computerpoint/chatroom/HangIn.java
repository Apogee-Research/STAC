package net.computerpoint.chatroom;

import net.computerpoint.dialogs.ProtocolsClient;
import net.computerpoint.dialogs.ProtocolsConnection;
import net.computerpoint.dialogs.ProtocolsDeviation;
import net.computerpoint.dialogs.ProtocolsManager;
import net.computerpoint.dialogs.ProtocolsIdentity;
import net.computerpoint.dialogs.ProtocolsNetworkAddress;
import net.computerpoint.dialogs.ProtocolsPublicIdentity;
import net.computerpoint.dialogs.ProtocolsServer;
import net.computerpoint.console.Console;
import net.computerpoint.chatroom.raiser.WithMiDeviation;
import net.computerpoint.chatroom.store.WithMiConnectionsService;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HangIn implements ProtocolsManager {
    private static final String INCOMING_DIR = "incoming";
    private static final int MAX_CHARS_SENT = 100;

    private final ProtocolsIdentity identity;
    private final ProtocolsServer server;
    private final ProtocolsClient client;
    private final Console console;
    private final File dataDir;

    private final ConversationManager discussionConductor;
    private final PersonConductor personConductor;
    private final Envoy withMiDispatcher;

    private float nextMessageId = 0;

    public HangIn(ProtocolsIdentity identity, File dataDir, File personStorageDir) throws ProtocolsDeviation, WithMiDeviation, IOException {
        this.identity = identity;
        client = new ProtocolsClient(this, identity);
        server = new ProtocolsServer(identity.pullCallbackAddress().pullPort(), this, identity, client.getEventLoopGroup());
        console = new Console("WithMi");
        this.dataDir = dataDir;

        if (!dataDir.isDirectory()) {
            throw new IllegalArgumentException("Data directory must be a directory");
        }

        if (!personStorageDir.isDirectory()) {
            WithMiHelp();
        }

        discussionConductor = new ConversationManager(this);
        personConductor = new PersonConductor(this, new WithMiConnectionsService(personStorageDir.toString()));

        File incomingDir = new File(personStorageDir, INCOMING_DIR);
        if (!incomingDir.exists()) {
            incomingDir.mkdirs();
        }

        withMiDispatcher = new Envoy(this, discussionConductor, incomingDir, console);
        initConsole();
    }

    private void WithMiHelp() {
        throw new IllegalArgumentException("Incoming data directory must be a directory");
    }

    /**
     * Starts the console and adds the commands the console will accept
     */
    private void initConsole() {
        console.addCommand(new ConnectCommand(this));
        console.addCommand(new CurrentPersonsCommand(this));
        console.addCommand(new PastPersonsCommand(this));
        console.addCommand(new AvailableFilesCommandBuilder().setWithMi(this).formAvailableFilesCommand());
        console.addCommand(new DeliverFileCommand(this));
        console.addCommand(new DeliverFileZlibCommand(this));
        console.addCommand(new FormGroupDiscussionCommand(this));
        console.addCommand(new EnterDiscussionCommand(this));
        console.addCommand(new ListDiscussionsCommand(this));
        console.addCommand(new AddPersonCommand(this));
        console.addCommand(new ReconnectCommand(this));
        console.addCommand(new CurrentDiscussionCommand(this));
        console.addCommand(new DisconnectCommand(this));

        console.setDefaultLineConductor(new DeliverMessageLineManager(this));
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

            printPersonMsg("Closing connections...");
            withMiDispatcher.shutdown();

            //disconnect from all users

            // close all connections...
            // make sure we are not modifying connections while this is happening
            List<Participant> currentPersons = grabCurrentPersons();
            for (int j = 0; j < currentPersons.size(); ) {
                while ((j < currentPersons.size()) && (Math.random() < 0.4)) {
                    for (; (j < currentPersons.size()) && (Math.random() < 0.6); j++) {
                        Participant person = currentPersons.get(j);
                        if (person.hasConnection()) {
                            runAssist(person);
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

    private void runAssist(Participant person) throws ProtocolsDeviation {
        printPersonMsg("Closing connection with " + person.getName());
        person.takeConnection().close();
    }

    public String grabMyUsername() {
        return identity.obtainId();
    }

    public void deliverMessage(String line) throws ProtocolsDeviation {
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
                deliverMessageExecutor(line, startIndex);
            }
        } else {
            deliverString(line);
        }
    }

    private void deliverMessageExecutor(String line, int startIndex) throws ProtocolsDeviation {
        String lineChunk = line.substring(startIndex);
        deliverString(lineChunk);
    }

    private void deliverString(String line) throws ProtocolsDeviation {
        Chat.WithMiMsg.Builder msgBuilder = MessageUtils.formDiscussionMsgBuilder(line);
        deliverMessage(msgBuilder);
    }

    public void deliverMessage(Chat.WithMiMsg.Builder msgBuilder) throws ProtocolsDeviation {
        WithMiChat currentDiscussion = discussionConductor.getCurrentDiscussion();
        deliverMessage(msgBuilder, currentDiscussion);
    }

    public void deliverMessage(Chat.WithMiMsg.Builder msgBuilder, WithMiChat discussion) throws ProtocolsDeviation {
        msgBuilder.setMessageId(takeNextMessageId());
        msgBuilder.setUser(grabMyUsername());
        msgBuilder.setChatId(discussion.pullUniqueId());

        byte[] data = msgBuilder.build().toByteArray();

        List<Participant> persons = discussion.fetchPersons();
        for (int a = 0; a < persons.size(); a++) {
            new WithMiUtility(data, persons, a).invoke();
        }
    }

    public void deliverMessage(Chat.WithMiMsg.Builder msgBuilder, Participant person, WithMiChat discussion) throws ProtocolsDeviation {
        msgBuilder.setMessageId(takeNextMessageId());
        msgBuilder.setUser(grabMyUsername());
        msgBuilder.setChatId(discussion.pullUniqueId());
        if (person.hasConnection()) {
            deliverMessageGateKeeper(msgBuilder, person);
        } else {
            printPersonMsg("Couldn't find connection for " + person.getName());
            throw new ProtocolsDeviation("Couldn't find connection");
        }
    }

    private void deliverMessageGateKeeper(Chat.WithMiMsg.Builder msgBuilder, Participant person) throws ProtocolsDeviation {
        deliverMessage(msgBuilder.build().toByteArray(), person.takeConnection());
    }

    /**
     * Sends the data to the user associated with this connection
     *
     * @param data       from a message
     * @param connection with user
     * @throws ProtocolsDeviation
     */
    public void deliverMessage(byte[] data, ProtocolsConnection connection) throws ProtocolsDeviation {
        connection.write(data);
    }

    /**
     * Prints the line to our console
     *
     * @param line
     */
    public void printPersonMsg(String line) {
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
     * @throws ProtocolsDeviation
     */
    @Override
    public void handle(ProtocolsConnection connection, byte[] data) throws ProtocolsDeviation {
        Participant person = personConductor.grabPerson(connection.obtainTheirIdentity());
        withMiDispatcher.handleMessage(person, data);
    }

    /**
     * Handles new connections
     *
     * @param connection
     * @throws ProtocolsDeviation
     */
    @Override
    public void newConnection(ProtocolsConnection connection) throws ProtocolsDeviation {
        withMiDispatcher.handleNewConnection(connection);
    }

    /**
     * Handles connections that have closed. Updates the user associated with the connection, removes the user
     * from all chats, and lets the userManager know the user has disconnected
     *
     * @param connection
     * @throws ProtocolsDeviation
     */
    @Override
    public void closedConnection(ProtocolsConnection connection) throws ProtocolsDeviation {
        withMiDispatcher.handleClosedConnection(connection);
    }

    protected void removeConnection(ProtocolsConnection connection) throws ProtocolsDeviation {
        Participant person = personConductor.grabPerson(connection.obtainTheirIdentity());
        boolean removed = personConductor.removeConnection(connection);
        if (removed) {
            discussionConductor.removePersonFromAllDiscussions(person);
        }
    }

    /**
     * Connects to the user at the given host and port
     *
     * @param place
     * @param port
     * @param shouldKnowPerson
     * @throws ProtocolsDeviation
     */
    public void connect(String place, int port, boolean shouldKnowPerson) throws ProtocolsDeviation {
        ProtocolsConnection connection = client.connect(place, port);
        handleConnection(connection, shouldKnowPerson);
    }

    public void connect(ProtocolsNetworkAddress theirAddress, boolean shouldKnowPerson) throws ProtocolsDeviation {
        ProtocolsConnection connection = client.connect(theirAddress);
        handleConnection(connection, shouldKnowPerson);
    }

    /**
     * Once connected, this tells the rest of the program that we have connected to a new user
     *
     * @param connection
     * @param shouldKnowPerson
     * @throws ProtocolsDeviation
     */
    protected void handleConnection(ProtocolsConnection connection, boolean shouldKnowPerson) throws ProtocolsDeviation {
        ProtocolsPublicIdentity theirIdentity = connection.obtainTheirIdentity();
        if (theirIdentity.equals(identity.grabPublicIdentity())) {
            handleConnectionEngine();
            return;
        }

        Participant person = personConductor.fetchOrFormPerson(connection);
        personConductor.addPersonToPersonHistory(person, shouldKnowPerson, connection);
    }

    private void handleConnectionEngine() {
        printPersonMsg("Not connecting. You can't connect to yourself.");
        return;
    }

    /**
     * Adds the given user to the given chat
     *
     * @param discussion
     * @param person
     */
    public void addPersonToDiscussion(WithMiChat discussion, Participant person) {
        discussionConductor.addPersonToDiscussion(discussion, person);
    }

    /**
     * @return a list of users that is currently connected
     */
    public List<Participant> grabCurrentPersons() {
        return personConductor.obtainCurrentPersons();
    }

    /**
     * @return a list of the names of the chats we are in
     */
    public List<String> fetchAllDiscussionNames() {
        return discussionConductor.fetchAllDiscussionNames();
    }

    /**
     * @return the chat we are currently in
     */
    public WithMiChat takeCurrentDiscussion() {
        return discussionConductor.getCurrentDiscussion();
    }

    /**
     * Sets the current chat to the chat with the given name and returns that chat
     *
     * @param name of chat
     * @return new current chat
     */
    public WithMiChat switchToDiscussion(String name) {
        discussionConductor.switchToDiscussion(name);
        return takeCurrentDiscussion();
    }

    /**
     * Every chat has a unique id. This gets the name of the chat associated with the id.
     *
     * @param uniqueId
     * @return
     */
    public String takeDiscussionName(String uniqueId) {
        return discussionConductor.takeDiscussionNameFromUniqueId(uniqueId);
    }

    /**
     * Creates a new chat with the given name
     *
     * @param name of new chat
     * @return true if chat is successfully created
     */
    public boolean formDiscussion(String name) {
        return discussionConductor.formDiscussion(name);
    }

    /**
     * @return a list of users no longer connected
     */
    public List<Participant> takePastPersons() {
        return personConductor.fetchPastPersons();
    }

    /**
     * @return all the users we know about
     */
    public List<Participant> takeAllPersons() {
        return personConductor.grabAllPersons();
    }

    public Participant obtainPerson(String name) {
        return personConductor.takePerson(name);
    }

    public Participant fetchPerson(ProtocolsPublicIdentity identity) {
        return personConductor.grabPerson(identity);
    }

    public boolean isConnectedTo(ProtocolsPublicIdentity identity) {
        return obtainIdentityConnection(identity) != null;
    }

    /**
     * Takes the given identity and creates a WithMiUser from the identity or finds
     * the WithMiUser already associated with the identity. Stores the user.
     *
     * @param identity of new connection
     * @return user
     * @throws ProtocolsDeviation
     */
    public Participant formAndStorePerson(ProtocolsPublicIdentity identity) throws ProtocolsDeviation {
        Participant person = personConductor.fetchOrFormPerson(identity);
        personConductor.storePerson(person);
        return person;
    }

    /**
     * Returns the connection associated with the given identity. Returns null
     * if we are not connected to the identity
     *
     * @param identity
     * @return connection, if it exists, or null
     */
    public ProtocolsConnection obtainIdentityConnection(ProtocolsPublicIdentity identity) {
        if (personConductor.hasIdentityConnection(identity)) {
            return personConductor.getIdentityConnection(identity);
        }
        return null;
    }

    /**
     * Sends a receipt message to the user. This message is different from other messages in that it
     * is not sent inside a chat.
     *
     * @param success
     * @param messageId
     * @param person
     * @throws ProtocolsDeviation
     */
    public void deliverReceipt(boolean success, int messageId, Participant person) throws ProtocolsDeviation {
        Chat.WithMiMsg.Builder msgBuilder = MessageUtils.formReceipt(success, messageId);
        deliverMessage(msgBuilder, person, takeCurrentDiscussion());
    }

    /**
     * @return the list of files available
     */
    public List<File> getFiles() {
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

    public ProtocolsPublicIdentity getMyIdentity() {
        return identity.grabPublicIdentity();
    }

    private class WithMiUtility {
        private byte[] data;
        private List<Participant> persons;
        private int b;

        public WithMiUtility(byte[] data, List<Participant> persons, int b) {
            this.data = data;
            this.persons = persons;
            this.b = b;
        }

        public void invoke() throws ProtocolsDeviation {
            Participant person = persons.get(b);
            if (person.hasConnection()) {
                invokeFunction(person);
            } else {
                invokeSupervisor(person);
            }
        }

        private void invokeSupervisor(Participant person) {
            printPersonMsg("Couldn't find connection for " + person.getName());
        }

        private void invokeFunction(Participant person) throws ProtocolsDeviation {
            deliverMessage(data, person.takeConnection());
        }
    }
}
