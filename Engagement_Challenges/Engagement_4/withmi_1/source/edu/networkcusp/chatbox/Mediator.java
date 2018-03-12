package edu.networkcusp.chatbox;

import edu.networkcusp.protocols.CommunicationsConnection;
import edu.networkcusp.protocols.CommunicationsFailure;
import edu.networkcusp.terminal.Console;

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
public class Mediator {
    private final HangIn withMi;
    private final ConversationManager discussionConductor;
    private final File incomingDir;
    private final Console console;

    private final Queue<FutureTask<?>> futureTaskQueue;
    private final ExecutorService executorService;

    public Mediator(HangIn withMi, ConversationManager discussionConductor, File incomingDir, Console console) {
        this.withMi = withMi;
        this.discussionConductor = discussionConductor;
        this.incomingDir = incomingDir;
        this.console = console;

        futureTaskQueue = new ConcurrentLinkedQueue<>();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void handleMessage(WithMiUser member, byte[] message) throws CommunicationsFailure {
        futureTaskQueue.add(new FutureTask<>(new MessageGuide(member, message)));
    }

    public void handleCommand(String line) {
        try {
            FutureTask<?> futureTask = new FutureTask<>(new CommandGuide(line));
            futureTaskQueue.add(futureTask);
            // Need to wait for this command (and any pending messages)
            // to get executed before allowing another command
            futureTask.get();
        } catch (ExecutionException e) {
            handleError(new CommunicationsFailure(e.getCause()));
        } catch (Exception e) {
            handleError(e);
        }
    }

    public void handleNewConnection(CommunicationsConnection connection) {
        futureTaskQueue.add(new FutureTask<>(new NewConnectionGuide(connection)));
    }

    public void handleClosedConnection(CommunicationsConnection connection) {
        futureTaskQueue.add(new FutureTask<>(new ClosedConnectionGuide(connection)));
    }

    public void handleError(Exception failure) {
        futureTaskQueue.add(new FutureTask<>(new ErrorGuide(failure)));
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
                try {
                    futureTask.run();
                    futureTask.get();
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof Exception) {
                        runAdviser(e);
                    } else {
                        throw e.getCause();
                    }
                }
            }
        }
    }

    private void runAdviser(ExecutionException e) throws CommunicationsFailure {
        throw new CommunicationsFailure(e.getCause());
    }

    /**
     * Handles any remaining console messages, then shuts down
     */
    public void shutdown() {
        console.assignShouldExit(true);
        executorService.shutdown();
    }

    private class ConsoleRunner implements Runnable {
        @Override
        public void run() {
            while (!console.shouldExit()) {
                runAdviser();
            }
        }

        private void runAdviser() {
            try {
                handleCommand(console.takeNextCommand());
            } catch (Exception e) {
                handleError(e);
            }
        }
    }

    private class ClosedConnectionGuide implements Callable<Void> {
        private final CommunicationsConnection connection;

        public ClosedConnectionGuide(CommunicationsConnection connection) {
            this.connection = connection;
        }

        @Override
        public Void call() {
            try {
                withMi.removeConnection(connection);
            } catch (CommunicationsFailure e) {
                System.err.println("Error handling closed connection " + e.getMessage());
            }
            return null;
        }
    }

    private class NewConnectionGuide implements Callable<Void> {
        private final CommunicationsConnection connection;

        public NewConnectionGuide(CommunicationsConnection connection) {
            this.connection = connection;
        }

        @Override
        public Void call() {
            try {
                withMi.handleConnection(connection, false);
            } catch (CommunicationsFailure e) {
                System.err.println("Error handling new connection " + e.getMessage());
            }
            return null;
        }
    }

    /**
     * Handles the commands that we receive
     */
    private class CommandGuide implements Callable<Void> {
        private final String command;

        public CommandGuide(String command) {
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
    private class MessageGuide implements Callable<Void> {
        private final WithMiUser member;
        private final byte[] data;

        public MessageGuide(WithMiUser member, byte[] data) {
            this.member = member;
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
                            FileTransfer receiver = new FileTransferBuilder().setWithMi(withMi).createFileTransfer();
                            receiver.receive(msg, incomingDir);
                            handleSuccess = true;
                            break;
                        case CHAT:
                            discussionConductor.handleDiscussionMessage(member.obtainName(), msg.getTextMsg(), discussionId);
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
                String discussionName = discussionConductor.pullDiscussionNameFromUniqueId(discussionId);
                WithMiChat discussion = discussionConductor.obtainDiscussion(discussionName);

                // we still want to send a receipt, even if we didn't handle it properly.
                if (deliverReceipt) {
                    Chat.WithMiMsg.Builder msgBuilder = MessageUtils.createReceipt(handleSuccess, msg.getMessageId());
                    withMi.deliverMessage(msgBuilder, member, discussion);
                }
            } catch (Exception e) {
                System.err.println("Error parsing message: " + e.getMessage());
            }
            return null;
        }
    }

    private class ErrorGuide implements Callable<Void> {
        private final Exception failure;

        private ErrorGuide(Exception failure) {
            this.failure = failure;
        }

        @Override
        public Void call() throws Exception {
            throw failure;
        }
    }
}
