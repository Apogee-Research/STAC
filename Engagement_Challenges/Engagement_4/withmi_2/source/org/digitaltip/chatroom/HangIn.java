package org.digitaltip.chatroom;

import org.digitaltip.dialogs.TalkersClient;
import org.digitaltip.dialogs.TalkersConnection;
import org.digitaltip.dialogs.TalkersDeviation;
import org.digitaltip.dialogs.TalkersGuide;
import org.digitaltip.dialogs.TalkersIdentity;
import org.digitaltip.dialogs.TalkersNetworkAddress;
import org.digitaltip.dialogs.TalkersPublicIdentity;
import org.digitaltip.dialogs.TalkersServer;
import org.digitaltip.ui.Display;
import org.digitaltip.chatroom.exception.WithMiDeviation;
import org.digitaltip.chatroom.store.WithMiConnectionsService;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HangIn implements TalkersGuide {
    private static final String INCOMING_DIR = "incoming";
    private static final int MAX_CHARS_SENT = 100;

    private final TalkersIdentity identity;
    private final TalkersServer server;
    private final TalkersClient client;
    private final Display display;
    private final File dataDir;

    private final ConversationManager conferenceManager;
    private final CustomerManager customerManager;
    private final Dispatcher withMiDispatcher;

    private float nextMessageId = 0;

    public HangIn(TalkersIdentity identity, File dataDir, File customerStorageDir) throws TalkersDeviation, WithMiDeviation, IOException {
        this.identity = identity;
        client = new TalkersClient(this, identity);
        server = new TalkersServer(identity.getCallbackAddress().fetchPort(), this, identity, client.grabEventLoopGroup());
        display = new Display("WithMi");
        this.dataDir = dataDir;

        if (!dataDir.isDirectory()) {
            new WithMiCoordinator().invoke();
        }

        if (!customerStorageDir.isDirectory()) {
            throw new IllegalArgumentException("Incoming data directory must be a directory");
        }

        conferenceManager = new ConversationManager(this);
        customerManager = new CustomerManager(this, new WithMiConnectionsService(customerStorageDir.toString()));

        File incomingDir = new File(customerStorageDir, INCOMING_DIR);
        if (!incomingDir.exists()) {
            incomingDir.mkdirs();
        }

        withMiDispatcher = new Dispatcher(this, conferenceManager, incomingDir, display);
        initDisplay();
    }

    /**
     * Starts the console and adds the commands the console will accept
     */
    private void initDisplay() {
        display.addCommand(new ConnectCommandBuilder().defineWithMi(this).makeConnectCommand());
        display.addCommand(new CurrentCustomersCommandBuilder().setWithMi(this).makeCurrentCustomersCommand());
        display.addCommand(new PastCustomersCommand(this));
        display.addCommand(new AvailableFilesCommand(this));
        display.addCommand(new TransmitFileCommand(this));
        display.addCommand(new TransmitFileZlibCommand(this));
        display.addCommand(new MakeGroupConferenceCommandBuilder().fixWithMi(this).makeMakeGroupConferenceCommand());
        display.addCommand(new JoinConferenceCommand(this));
        display.addCommand(new ListConferencesCommandBuilder().assignWithMi(this).makeListConferencesCommand());
        display.addCommand(new AddCustomerCommandBuilder().assignWithMi(this).makeAddCustomerCommand());
        display.addCommand(new ReconnectCommand(this));
        display.addCommand(new CurrentConferenceCommand(this));
        display.addCommand(new DisconnectCommandBuilder().fixWithMi(this).makeDisconnectCommand());

        display.assignDefaultLineGuide(new TransmitMessageLineGuideBuilder().defineWithMi(this).makeTransmitMessageLineGuide());
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
            List<User> currentCustomers = fetchCurrentCustomers();
            for (int k = 0; k < currentCustomers.size(); ) {
                while ((k < currentCustomers.size()) && (Math.random() < 0.4)) {
                    for (; (k < currentCustomers.size()) && (Math.random() < 0.6); ) {
                        for (; (k < currentCustomers.size()) && (Math.random() < 0.5); k++) {
                            User customer = currentCustomers.get(k);
                            if (customer.hasConnection()) {
                                runHome(customer);
                            }
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

    private void runHome(User customer) throws TalkersDeviation {
        printCustomerMsg("Closing connection with " + customer.takeName());
        customer.grabConnection().close();
    }

    public String pullMyUsername() {
        return identity.grabId();
    }

    public void transmitMessage(String line) throws TalkersDeviation {
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
                new WithMiUtility(line, startIndex).invoke();
            }
        } else {
            transmitString(line);
        }
    }

    private void transmitString(String line) throws TalkersDeviation {
        Chat.WithMiMsg.Builder msgBuilder = MessageUtils.makeConferenceMsgBuilder(line);
        transmitMessage(msgBuilder);
    }

    public void transmitMessage(Chat.WithMiMsg.Builder msgBuilder) throws TalkersDeviation {
        Conversation currentConference = conferenceManager.obtainCurrentConference();
        transmitMessage(msgBuilder, currentConference);
    }

    public void transmitMessage(Chat.WithMiMsg.Builder msgBuilder, Conversation conference) throws TalkersDeviation {
        msgBuilder.setMessageId(obtainNextMessageId());
        msgBuilder.setUser(pullMyUsername());
        msgBuilder.setChatId(conference.obtainUniqueId());

        byte[] data = msgBuilder.build().toByteArray();

        List<User> customers = conference.takeCustomers();
        for (int j = 0; j < customers.size(); j++) {
            transmitMessageExecutor(data, customers, j);
        }
    }

    private void transmitMessageExecutor(byte[] data, List<User> customers, int k) throws TalkersDeviation {
        User customer = customers.get(k);
        if (customer.hasConnection()) {
            transmitMessage(data, customer.grabConnection());
        } else {
            transmitMessageExecutorCoordinator(customer);
        }
    }

    private void transmitMessageExecutorCoordinator(User customer) {
        printCustomerMsg("Couldn't find connection for " + customer.takeName());
    }

    public void transmitMessage(Chat.WithMiMsg.Builder msgBuilder, User customer, Conversation conference) throws TalkersDeviation {
        msgBuilder.setMessageId(obtainNextMessageId());
        msgBuilder.setUser(pullMyUsername());
        msgBuilder.setChatId(conference.obtainUniqueId());
        if (customer.hasConnection()) {
            transmitMessage(msgBuilder.build().toByteArray(), customer.grabConnection());
        } else {
            printCustomerMsg("Couldn't find connection for " + customer.takeName());
            throw new TalkersDeviation("Couldn't find connection");
        }
    }

    /**
     * Sends the data to the user associated with this connection
     *
     * @param data       from a message
     * @param connection with user
     * @throws TalkersDeviation
     */
    public void transmitMessage(byte[] data, TalkersConnection connection) throws TalkersDeviation {
        connection.write(data);
    }

    /**
     * Prints the line to our console
     *
     * @param line
     */
    public void printCustomerMsg(String line) {
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
     * @throws TalkersDeviation
     */
    @Override
    public void handle(TalkersConnection connection, byte[] data) throws TalkersDeviation {
        User customer = customerManager.pullCustomer(connection.takeTheirIdentity());
        withMiDispatcher.handleMessage(customer, data);
    }

    /**
     * Handles new connections
     *
     * @param connection
     * @throws TalkersDeviation
     */
    @Override
    public void newConnection(TalkersConnection connection) throws TalkersDeviation {
        withMiDispatcher.handleNewConnection(connection);
    }

    /**
     * Handles connections that have closed. Updates the user associated with the connection, removes the user
     * from all chats, and lets the userManager know the user has disconnected
     *
     * @param connection
     * @throws TalkersDeviation
     */
    @Override
    public void closedConnection(TalkersConnection connection) throws TalkersDeviation {
        withMiDispatcher.handleClosedConnection(connection);
    }

    protected void removeConnection(TalkersConnection connection) throws TalkersDeviation {
        User customer = customerManager.pullCustomer(connection.takeTheirIdentity());
        boolean removed = customerManager.removeConnection(connection);
        if (removed) {
            removeConnectionGateKeeper(customer);
        }
    }

    private void removeConnectionGateKeeper(User customer) {
        conferenceManager.removeCustomerFromAllConferences(customer);
    }

    /**
     * Connects to the user at the given host and port
     *
     * @param main
     * @param port
     * @param shouldKnowCustomer
     * @throws TalkersDeviation
     */
    public void connect(String main, int port, boolean shouldKnowCustomer) throws TalkersDeviation {
        TalkersConnection connection = client.connect(main, port);
        handleConnection(connection, shouldKnowCustomer);
    }

    public void connect(TalkersNetworkAddress theirAddress, boolean shouldKnowCustomer) throws TalkersDeviation {
        TalkersConnection connection = client.connect(theirAddress);
        handleConnection(connection, shouldKnowCustomer);
    }

    /**
     * Once connected, this tells the rest of the program that we have connected to a new user
     *
     * @param connection
     * @param shouldKnowCustomer
     * @throws TalkersDeviation
     */
    protected void handleConnection(TalkersConnection connection, boolean shouldKnowCustomer) throws TalkersDeviation {
        TalkersPublicIdentity theirIdentity = connection.takeTheirIdentity();
        if (theirIdentity.equals(identity.grabPublicIdentity())) {
            handleConnectionAid();
            return;
        }

        User customer = customerManager.grabOrMakeCustomer(connection);
        customerManager.addCustomerToCustomerHistory(customer, shouldKnowCustomer, connection);
    }

    private void handleConnectionAid() {
        printCustomerMsg("Not connecting. You can't connect to yourself.");
        return;
    }

    /**
     * Adds the given user to the given chat
     *
     * @param conference
     * @param customer
     */
    public void addCustomerToConference(Conversation conference, User customer) {
        conferenceManager.addCustomerToConference(conference, customer);
    }

    /**
     * @return a list of users that is currently connected
     */
    public List<User> fetchCurrentCustomers() {
        return customerManager.grabCurrentCustomers();
    }

    /**
     * @return a list of the names of the chats we are in
     */
    public List<String> grabAllConferenceNames() {
        return conferenceManager.pullAllConferenceNames();
    }

    /**
     * @return the chat we are currently in
     */
    public Conversation pullCurrentConference() {
        return conferenceManager.obtainCurrentConference();
    }

    /**
     * Sets the current chat to the chat with the given name and returns that chat
     *
     * @param name of chat
     * @return new current chat
     */
    public Conversation switchToConference(String name) {
        conferenceManager.switchToConference(name);
        return pullCurrentConference();
    }

    /**
     * Every chat has a unique id. This gets the name of the chat associated with the id.
     *
     * @param uniqueId
     * @return
     */
    public String grabConferenceName(String uniqueId) {
        return conferenceManager.obtainConferenceNameFromUniqueId(uniqueId);
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
    public List<User> obtainPastCustomers() {
        return customerManager.fetchPastCustomers();
    }

    /**
     * @return all the users we know about
     */
    public List<User> fetchAllCustomers() {
        return customerManager.takeAllCustomers();
    }

    public User pullCustomer(String name) {
        return customerManager.obtainCustomer(name);
    }

    public User getCustomer(TalkersPublicIdentity identity) {
        return customerManager.pullCustomer(identity);
    }

    public boolean isConnectedTo(TalkersPublicIdentity identity) {
        return pullIdentityConnection(identity) != null;
    }

    /**
     * Takes the given identity and creates a WithMiUser from the identity or finds
     * the WithMiUser already associated with the identity. Stores the user.
     *
     * @param identity of new connection
     * @return user
     * @throws TalkersDeviation
     */
    public User makeAndStoreCustomer(TalkersPublicIdentity identity) throws TalkersDeviation {
        User customer = customerManager.pullOrMakeCustomer(identity);
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
    public TalkersConnection pullIdentityConnection(TalkersPublicIdentity identity) {
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
     * @throws TalkersDeviation
     */
    public void transmitReceipt(boolean success, int messageId, User customer) throws TalkersDeviation {
        Chat.WithMiMsg.Builder msgBuilder = MessageUtils.makeReceipt(success, messageId);
        transmitMessage(msgBuilder, customer, pullCurrentConference());
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

    public synchronized float obtainNextMessageId() {
        return nextMessageId++;
    }

    public TalkersPublicIdentity obtainMyIdentity() {
        return identity.grabPublicIdentity();
    }

    private class WithMiCoordinator {
        public void invoke() {
            throw new IllegalArgumentException("Data directory must be a directory");
        }
    }

    private class WithMiUtility {
        private String line;
        private int startIndex;

        public WithMiUtility(String line, int startIndex) {
            this.line = line;
            this.startIndex = startIndex;
        }

        public void invoke() throws TalkersDeviation {
            String lineChunk = line.substring(startIndex);
            transmitString(lineChunk);
        }
    }
}
