package net.computerpoint.chatroom;

import net.computerpoint.dialogs.ProtocolsConnection;
import net.computerpoint.dialogs.ProtocolsDeviation;
import net.computerpoint.console.Console;

import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Handles the provided messages and console commands in the order that they are received
 */
public class Envoy {
    private final HangIn withMi;
    private final ConversationManager discussionConductor;
    private final File incomingDir;
    private final Console console;

    private final Queue<FutureTask<?>> futureTaskQueue;
    private final ExecutorService executorService;

    public Envoy(HangIn withMi, ConversationManager discussionConductor, File incomingDir, Console console) {
        this.withMi = withMi;
        this.discussionConductor = discussionConductor;
        this.incomingDir = incomingDir;
        this.console = console;

        futureTaskQueue = new ConcurrentLinkedQueue<>();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void handleMessage(Participant person, byte[] message) throws ProtocolsDeviation {
        futureTaskQueue.add(new FutureTask<>(new MessageManager(person, message)));
    }

    public void handleCommand(String line) {
        try {
            FutureTask<?> futureTask = new FutureTask<>(new CommandManager(line));
            futureTaskQueue.add(futureTask);
            // Need to wait for this command (and any pending messages)
            // to get executed before allowing another command
            futureTask.get();
        } catch (ExecutionException e) {
            handleError(new ProtocolsDeviation(e.getCause()));
        } catch (Exception e) {
            handleError(e);
        }
    }

    public void handleNewConnection(ProtocolsConnection connection) {
        futureTaskQueue.add(new FutureTask<>(new NewConnectionManager(connection)));
    }

    public void handleClosedConnection(ProtocolsConnection connection) {
        futureTaskQueue.add(new FutureTask<>(new ClosedConnectionManager(connection)));
    }

    public void handleError(Exception deviation) {
        futureTaskQueue.add(new FutureTask<>(new ErrorManager(deviation)));
    }

    /**
     * Runs all commands and messages in the calling thread.
     * All console commands are parsed and launched in a
     * background thread so the processing of the commands
     * and messages can be handled in the calling thread.
     * This allows the caller to be notified of background
     * processing errors.
     *
     * @throws Throwable if there are processing errors
     */
    public void run() throws Throwable {
        executorService.execute(new ConsoleRunner());

        // While waiting for the console to conclude,
        // process the results of all background dispatches in the
        // main thread so the user can be notified of any issues.
        while (!console.shouldExit()) {
            FutureTask<?> futureTask;
            while ((futureTask = futureTaskQueue.poll()) != null) {
                runHome(futureTask);
            }
        }
    }

    private void runHome(FutureTask<?> futureTask) throws Throwable {
        try {
            futureTask.run();
            futureTask.get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof Exception) {
                throw new ProtocolsDeviation(e.getCause());
            } else {
                throw e.getCause();
            }
        }
    }

    /**
     * Handles any remaining console messages, then shuts down
     */
    public void shutdown() {
        console.setShouldExit(true);
        executorService.shutdown();
    }

    private class ConsoleRunner implements Runnable {
        @Override
        public void run() {
            while (!console.shouldExit()) {
                runGateKeeper();
            }
        }

        private void runGateKeeper() {
            try {
                handleCommand(console.pullNextCommand());
            } catch (Exception e) {
                handleError(e);
            }
        }
    }

    private class ClosedConnectionManager implements Callable<Void> {
        private final ProtocolsConnection connection;

        public ClosedConnectionManager(ProtocolsConnection connection) {
            this.connection = connection;
        }

        @Override
        public Void call() {
            try {
                withMi.removeConnection(connection);
            } catch (ProtocolsDeviation e) {
                System.err.println("Error handling closed connection " + e.getMessage());
            }
            return null;
        }
    }

    private class NewConnectionManager implements Callable<Void> {
        private final ProtocolsConnection connection;

        public NewConnectionManager(ProtocolsConnection connection) {
            this.connection = connection;
        }

        @Override
        public Void call() {
            try {
                withMi.handleConnection(connection, false);
            } catch (ProtocolsDeviation e) {
                System.err.println("Error handling new connection " + e.getMessage());
            }
            return null;
        }
    }

    /**
     * Handles the commands that we receive
     */
    private class CommandManager implements Callable<Void> {
        private final String command;

        public CommandManager(String command) {
            this.command = command;
        }

        @Override
        public Void call() {
            try {
                console.executeCommand(command);
            } catch (IOException e) {
                System.err.println("Error executing command: " + e.getMessage());
            }
            return null;
        }
    }

    /**
     * Handles the messages that we receive
     */
    private class MessageManager implements Callable<Void> {
        private final Participant person;
        private final byte[] data;

        public MessageManager(Participant person, byte[] data) {
            this.person = person;
            this.data = data;
        }

        @Override
        public Void call() {
            try {
                // parse the message
                Chat.WithMiMsg msg = Chat.WithMiMsg.parseFrom(data);
                String discussionId = msg.getChatId();

                boolean deliverReceipt = true;
                boolean handleSuccess = false;
                // determine what type of message we have and handle it accordingly
                try {
                    switch (msg.getType()) {
                        case FILE:
                            FileTransfer receiver = new FileTransfer(withMi);
                            receiver.receive(msg, incomingDir);
                            handleSuccess = true;
                            break;
                        case CHAT:
                            discussionConductor.handleDiscussionMessage(person.getName(), msg.getTextMsg(), discussionId);
                            handleSuccess = true;
                            break;
                        case CHAT_STATE:
                            String newDiscussionName = msg.getTextMsg().getTextMsg();
                            discussionConductor.handleDiscussionStateMessage(msg.getChatStateMsg(), discussionId, newDiscussionName);
                            handleSuccess = true;
                            break;
                        case READ_RECEIPT:
                            // don't send a receipt for a receipt
                            deliverReceipt = false;
                            handleSuccess = true;
                            break;
                    }
                } catch (Exception e) {
                    System.err.println("Error handling message: " + e.getMessage());
                }

                // reset the chat in case it has changed
                String discussionName = discussionConductor.takeDiscussionNameFromUniqueId(discussionId);
                WithMiChat discussion = discussionConductor.grabDiscussion(discussionName);

                // we still want to send a receipt, even if we didn't handle it properly.
                if (deliverReceipt) {
                    Chat.WithMiMsg.Builder msgBuilder = MessageUtils.formReceipt(handleSuccess, msg.getMessageId());
                    withMi.deliverMessage(msgBuilder, person, discussion);
                }
            } catch (Exception e) {
                System.err.println("Error parsing message: " + e.getMessage());
            }
            return null;
        }
    }

    private class ErrorManager implements Callable<Void> {
        private final Exception deviation;

        private ErrorManager(Exception deviation) {
            this.deviation = deviation;
        }

        @Override
        public Void call() throws Exception {
            throw deviation;
        }
    }
}
