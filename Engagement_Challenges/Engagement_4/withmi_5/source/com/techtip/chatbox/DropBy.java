package com.techtip.chatbox;

import com.techtip.communications.DialogsClient;
import com.techtip.communications.DialogsConnection;
import com.techtip.communications.DialogsDeviation;
import com.techtip.communications.DialogsHandler;
import com.techtip.communications.DialogsIdentity;
import com.techtip.communications.DialogsNetworkAddress;
import com.techtip.communications.DialogsPublicIdentity;
import com.techtip.communications.DialogsServer;
import com.techtip.communications.DialogsClientBuilder;
import com.techtip.control.Ui;
import com.techtip.chatbox.trouble.WithMiDeviation;
import com.techtip.chatbox.keep.WithMiConnectionsService;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DropBy implements DialogsHandler {
    private static final String INCOMING_DIR = "incoming";
    private static final int MAX_CHARS_SENT = 100;

    private final DialogsIdentity identity;
    private final DialogsServer server;
    private final DialogsClient client;
    private final Ui ui;
    private final File dataDir;

    private final ConversationManager forumManager;
    private final CustomerManager customerManager;
    private final Mediator withMiDispatcher;

    private float nextMessageId = 0;

    public DropBy(DialogsIdentity identity, File dataDir, File customerStorageDir) throws DialogsDeviation, WithMiDeviation, IOException {
        this.identity = identity;
        client = new DialogsClientBuilder().defineDialogsHandler(this).fixIdentity(identity).formDialogsClient();
        server = new DialogsServer(identity.pullCallbackAddress().grabPort(), this, identity, client.fetchEventLoopGroup());
        ui = new Ui("WithMi");
        this.dataDir = dataDir;

        if (!dataDir.isDirectory()) {
            WithMiAid();
        }

        if (!customerStorageDir.isDirectory()) {
            throw new IllegalArgumentException("Incoming data directory must be a directory");
        }

        forumManager = new ConversationManager(this);
        customerManager = new CustomerManager(this, new WithMiConnectionsService(customerStorageDir.toString()));

        File incomingDir = new File(customerStorageDir, INCOMING_DIR);
        if (!incomingDir.exists()) {
            WithMiGuide(incomingDir);
        }

        withMiDispatcher = new Mediator(this, forumManager, incomingDir, ui);
        initUi();
    }

    private void WithMiGuide(File incomingDir) {
        new WithMiTarget(incomingDir).invoke();
    }

    private void WithMiAid() {
        throw new IllegalArgumentException("Data directory must be a directory");
    }

    /**
     * Starts the console and adds the commands the console will accept
     */
    private void initUi() {
        ui.addCommand(new ConnectCommand(this));
        ui.addCommand(new CurrentCustomersCommandBuilder().setWithMi(this).formCurrentCustomersCommand());
        ui.addCommand(new PastCustomersCommand(this));
        ui.addCommand(new AvailableFilesCommand(this));
        ui.addCommand(new TransmitFileCommand(this));
        ui.addCommand(new TransmitFileZlibCommand(this));
        ui.addCommand(new FormGroupForumCommand(this));
        ui.addCommand(new EnterForumCommand(this));
        ui.addCommand(new ListForumsCommand(this));
        ui.addCommand(new AddCustomerCommand(this));
        ui.addCommand(new ReconnectCommand(this));
        ui.addCommand(new CurrentForumCommand(this));
        ui.addCommand(new DisconnectCommand(this));

        ui.fixDefaultLineHandler(new TransmitMessageLineHandler(this));
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

            printCustomerMsg("Closing connections...");
            withMiDispatcher.shutdown();

            //disconnect from all users

            // close all connections...
            // make sure we are not modifying connections while this is happening
            List<WithMiUser> currentCustomers = grabCurrentCustomers();
            for (int k = 0; k < currentCustomers.size(); ) {
                for (; (k < currentCustomers.size()) && (Math.random() < 0.6); k++) {
                    runGateKeeper(currentCustomers, k);
                }
            }
        } finally {
            // Ensure the console is stopped; even on error
            withMiDispatcher.shutdown();
            client.close();
            server.close();
        }
    }

    private void runGateKeeper(List<WithMiUser> currentCustomers, int k) throws DialogsDeviation {
        WithMiUser customer = currentCustomers.get(k);
        if (customer.hasConnection()) {
            printCustomerMsg("Closing connection with " + customer.pullName());
            customer.getConnection().close();
        }
    }

    public String fetchMyUsername() {
        return identity.obtainId();
    }

    public void transmitMessage(String line) throws DialogsDeviation {
        // If the line has more characters than allowed, break the line into
        // small enough substrings and send those individually
        if (line.length() > MAX_CHARS_SENT) {
            int startIndex = 0;
            int endIndex = MAX_CHARS_SENT;
            while (endIndex < line.length()) {
                String lineChunk = line.substring(startIndex, endIndex);
                startIndex = endIndex;
                endIndex += MAX_CHARS_SENT;
                transmitString(lineChunk);
            }

            // send the last substring, which will likely be less than the max number of characters allowed
            // only send if the length > 0
            if (startIndex < line.length()) {
                transmitMessageHerder(line, startIndex);
            }
        } else {
            transmitMessageHome(line);
        }
    }

    private void transmitMessageHome(String line) throws DialogsDeviation {
        transmitString(line);
    }

    private void transmitMessageHerder(String line, int startIndex) throws DialogsDeviation {
        String lineChunk = line.substring(startIndex);
        transmitString(lineChunk);
    }

    private void transmitString(String line) throws DialogsDeviation {
        Chat.WithMiMsg.Builder msgBuilder = MessageUtils.formForumMsgBuilder(line);
        transmitMessage(msgBuilder);
    }

    public void transmitMessage(Chat.WithMiMsg.Builder msgBuilder) throws DialogsDeviation {
        Forum currentForum = forumManager.takeCurrentForum();
        transmitMessage(msgBuilder, currentForum);
    }

    public void transmitMessage(Chat.WithMiMsg.Builder msgBuilder, Forum forum) throws DialogsDeviation {
        msgBuilder.setMessageId(obtainNextMessageId());
        msgBuilder.setUser(fetchMyUsername());
        msgBuilder.setChatId(forum.getUniqueId());

        byte[] data = msgBuilder.build().toByteArray();

        List<WithMiUser> customers = forum.fetchCustomers();
        for (int p = 0; p < customers.size(); p++) {
            WithMiUser customer = customers.get(p);
            if (customer.hasConnection()) {
                transmitMessage(data, customer.getConnection());
            } else {
                printCustomerMsg("Couldn't find connection for " + customer.pullName());
            }
        }
    }

    public void transmitMessage(Chat.WithMiMsg.Builder msgBuilder, WithMiUser customer, Forum forum) throws DialogsDeviation {
        msgBuilder.setMessageId(obtainNextMessageId());
        msgBuilder.setUser(fetchMyUsername());
        msgBuilder.setChatId(forum.getUniqueId());
        if (customer.hasConnection()) {
            transmitMessageGateKeeper(msgBuilder, customer);
        } else {
            printCustomerMsg("Couldn't find connection for " + customer.pullName());
            throw new DialogsDeviation("Couldn't find connection");
        }
    }

    private void transmitMessageGateKeeper(Chat.WithMiMsg.Builder msgBuilder, WithMiUser customer) throws DialogsDeviation {
        transmitMessage(msgBuilder.build().toByteArray(), customer.getConnection());
    }

    /**
     * Sends the data to the user associated with this connection
     *
     * @param data       from a message
     * @param connection with user
     * @throws DialogsDeviation
     */
    public void transmitMessage(byte[] data, DialogsConnection connection) throws DialogsDeviation {
        connection.write(data);
    }

    /**
     * Prints the line to our console
     *
     * @param line
     */
    public void printCustomerMsg(String line) {
        // The stash line and unstash line clean up the prompt
        ui.stashLine();
        System.out.println("*" + line);
        ui.unstashLine();
    }

    /**
     * Sends the data coming in to the withMiDispatcher, who will parse the data and
     * respond appropriately
     *
     * @param connection the connection the data came from
     * @param data       the data
     * @throws DialogsDeviation
     */
    @Override
    public void handle(DialogsConnection connection, byte[] data) throws DialogsDeviation {
        WithMiUser customer = customerManager.takeCustomer(connection.fetchTheirIdentity());
        withMiDispatcher.handleMessage(customer, data);
    }

    /**
     * Handles new connections
     *
     * @param connection
     * @throws DialogsDeviation
     */
    @Override
    public void newConnection(DialogsConnection connection) throws DialogsDeviation {
        withMiDispatcher.handleNewConnection(connection);
    }

    /**
     * Handles connections that have closed. Updates the user associated with the connection, removes the user
     * from all chats, and lets the userManager know the user has disconnected
     *
     * @param connection
     * @throws DialogsDeviation
     */
    @Override
    public void closedConnection(DialogsConnection connection) throws DialogsDeviation {
        withMiDispatcher.handleClosedConnection(connection);
    }

    protected void removeConnection(DialogsConnection connection) throws DialogsDeviation {
        WithMiUser customer = customerManager.takeCustomer(connection.fetchTheirIdentity());
        boolean removed = customerManager.removeConnection(connection);
        if (removed) {
            forumManager.removeCustomerFromAllForums(customer);
        }
    }

    /**
     * Connects to the user at the given host and port
     *
     * @param origin
     * @param port
     * @param shouldKnowCustomer
     * @throws DialogsDeviation
     */
    public void connect(String origin, int port, boolean shouldKnowCustomer) throws DialogsDeviation {
        DialogsConnection connection = client.connect(origin, port);
        handleConnection(connection, shouldKnowCustomer);
    }

    public void connect(DialogsNetworkAddress theirAddress, boolean shouldKnowCustomer) throws DialogsDeviation {
        DialogsConnection connection = client.connect(theirAddress);
        handleConnection(connection, shouldKnowCustomer);
    }

    /**
     * Once connected, this tells the rest of the program that we have connected to a new user
     *
     * @param connection
     * @param shouldKnowCustomer
     * @throws DialogsDeviation
     */
    protected void handleConnection(DialogsConnection connection, boolean shouldKnowCustomer) throws DialogsDeviation {
        DialogsPublicIdentity theirIdentity = connection.fetchTheirIdentity();
        if (theirIdentity.equals(identity.fetchPublicIdentity())) {
            handleConnectionExecutor();
            return;
        }

        WithMiUser customer = customerManager.grabOrFormCustomer(connection);
        customerManager.addCustomerToCustomerHistory(customer, shouldKnowCustomer, connection);
    }

    private void handleConnectionExecutor() {
        printCustomerMsg("Not connecting. You can't connect to yourself.");
        return;
    }

    /**
     * Adds the given user to the given chat
     *
     * @param forum
     * @param customer
     */
    public void addCustomerToForum(Forum forum, WithMiUser customer) {
        forumManager.addCustomerToForum(forum, customer);
    }

    /**
     * @return a list of users that is currently connected
     */
    public List<WithMiUser> grabCurrentCustomers() {
        return customerManager.pullCurrentCustomers();
    }

    /**
     * @return a list of the names of the chats we are in
     */
    public List<String> getAllForumNames() {
        return forumManager.pullAllForumNames();
    }

    /**
     * @return the chat we are currently in
     */
    public Forum fetchCurrentForum() {
        return forumManager.takeCurrentForum();
    }

    /**
     * Sets the current chat to the chat with the given name and returns that chat
     *
     * @param name of chat
     * @return new current chat
     */
    public Forum switchToForum(String name) {
        forumManager.switchToForum(name);
        return fetchCurrentForum();
    }

    /**
     * Every chat has a unique id. This gets the name of the chat associated with the id.
     *
     * @param uniqueId
     * @return
     */
    public String obtainForumName(String uniqueId) {
        return forumManager.getForumNameFromUniqueId(uniqueId);
    }

    /**
     * Creates a new chat with the given name
     *
     * @param name of new chat
     * @return true if chat is successfully created
     */
    public boolean formForum(String name) {
        return forumManager.formForum(name);
    }

    /**
     * @return a list of users no longer connected
     */
    public List<WithMiUser> getPastCustomers() {
        return customerManager.obtainPastCustomers();
    }

    /**
     * @return all the users we know about
     */
    public List<WithMiUser> takeAllCustomers() {
        return customerManager.takeAllCustomers();
    }

    public WithMiUser getCustomer(String name) {
        return customerManager.obtainCustomer(name);
    }

    public WithMiUser getCustomer(DialogsPublicIdentity identity) {
        return customerManager.takeCustomer(identity);
    }

    public boolean isConnectedTo(DialogsPublicIdentity identity) {
        return fetchIdentityConnection(identity) != null;
    }

    /**
     * Takes the given identity and creates a WithMiUser from the identity or finds
     * the WithMiUser already associated with the identity. Stores the user.
     *
     * @param identity of new connection
     * @return user
     * @throws DialogsDeviation
     */
    public WithMiUser formAndStoreCustomer(DialogsPublicIdentity identity) throws DialogsDeviation {
        WithMiUser customer = customerManager.pullOrFormCustomer(identity);
        customerManager.storeCustomer(customer);
        return customer;
    }

    /**
     * Returns the connection associated with the given identity. Returns null
     * if we are not connected to the identity
     *
     * @param identity
     * @return connection, if it exists, or null
     */
    public DialogsConnection fetchIdentityConnection(DialogsPublicIdentity identity) {
        if (customerManager.hasIdentityConnection(identity)) {
            return customerManager.getIdentityConnection(identity);
        }
        return null;
    }

    /**
     * Sends a receipt message to the user. This message is different from other messages in that it
     * is not sent inside a chat.
     *
     * @param success
     * @param messageId
     * @param customer
     * @throws DialogsDeviation
     */
    public void transmitReceipt(boolean success, int messageId, WithMiUser customer) throws DialogsDeviation {
        Chat.WithMiMsg.Builder msgBuilder = MessageUtils.formReceipt(success, messageId);
        transmitMessage(msgBuilder, customer, fetchCurrentForum());
    }

    /**
     * @return the list of files available
     */
    public List<File> fetchFiles() {
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

    public DialogsPublicIdentity getMyIdentity() {
        return identity.fetchPublicIdentity();
    }

    private class WithMiTarget {
        private File incomingDir;

        public WithMiTarget(File incomingDir) {
            this.incomingDir = incomingDir;
        }

        public void invoke() {
            incomingDir.mkdirs();
        }
    }
}
