package org.digitaltip.chatroom;

import org.digitaltip.dialogs.TalkersConnection;
import org.digitaltip.dialogs.TalkersDeviation;
import org.digitaltip.ui.Display;

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
public class Dispatcher {
    private final HangIn withMi;
    private final ConversationManager conferenceManager;
    private final File incomingDir;
    private final Display display;

    private final Queue<FutureTask<?>> futureTaskQueue;
    private final ExecutorService executorService;

    public Dispatcher(HangIn withMi, ConversationManager conferenceManager, File incomingDir, Display display) {
        this.withMi = withMi;
        this.conferenceManager = conferenceManager;
        this.incomingDir = incomingDir;
        this.display = display;

        futureTaskQueue = new ConcurrentLinkedQueue<>();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void handleMessage(User customer, byte[] message) throws TalkersDeviation {
        futureTaskQueue.add(new FutureTask<>(new MessageGuide(customer, message)));
    }

    public void handleCommand(String line) {
        try {
            FutureTask<?> futureTask = new FutureTask<>(new CommandGuide(line));
            futureTaskQueue.add(futureTask);
            // Need to wait for this command (and any pending messages)
            // to get executed before allowing another command
            futureTask.get();
        } catch (ExecutionException e) {
            handleError(new TalkersDeviation(e.getCause()));
        } catch (Exception e) {
            handleError(e);
        }
    }

    public void handleNewConnection(TalkersConnection connection) {
        futureTaskQueue.add(new FutureTask<>(new NewConnectionGuide(connection)));
    }

    public void handleClosedConnection(TalkersConnection connection) {
        futureTaskQueue.add(new FutureTask<>(new ClosedConnectionGuide(connection)));
    }

    public void handleError(Exception deviation) {
        futureTaskQueue.add(new FutureTask<>(new ErrorGuide(deviation)));
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
        executorService.execute(new DisplayRunner());

        // While waiting for the console to conclude,
        // process the results of all background dispatches in the
        // main thread so the user can be notified of any issues.
        while (!display.shouldExit()) {
            FutureTask<?> futureTask;
            while ((futureTask = futureTaskQueue.poll()) != null) {
                runFunction(futureTask);
            }
        }
    }

    private void runFunction(FutureTask<?> futureTask) throws Throwable {
        try {
            futureTask.run();
            futureTask.get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof Exception) {
                throw new TalkersDeviation(e.getCause());
            } else {
                throw e.getCause();
            }
        }
    }

    /**
     * Handles any remaining console messages, then shuts down
     */
    public void shutdown() {
        display.fixShouldExit(true);
        executorService.shutdown();
    }

    private class DisplayRunner implements Runnable {
        @Override
        public void run() {
            while (!display.shouldExit()) {
                try {
                    handleCommand(display.obtainNextCommand());
                } catch (Exception e) {
                    handleError(e);
                }
            }
        }
    }

    private class ClosedConnectionGuide implements Callable<Void> {
        private final TalkersConnection connection;

        public ClosedConnectionGuide(TalkersConnection connection) {
            this.connection = connection;
        }

        @Override
        public Void call() {
            try {
                withMi.removeConnection(connection);
            } catch (TalkersDeviation e) {
                System.err.println("Error handling closed connection " + e.getMessage());
            }
            return null;
        }
    }

    private class NewConnectionGuide implements Callable<Void> {
        private final TalkersConnection connection;

        public NewConnectionGuide(TalkersConnection connection) {
            this.connection = connection;
        }

        @Override
        public Void call() {
            try {
                withMi.handleConnection(connection, false);
            } catch (TalkersDeviation e) {
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
                display.executeCommand(command);
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
        private final User customer;
        private final byte[] data;

        public MessageGuide(User customer, byte[] data) {
            this.customer = customer;
            this.data = data;
        }

        @Override
        public Void call() {
            try {
                // parse the message
                Chat.WithMiMsg msg = Chat.WithMiMsg.parseFrom(data);
                String conferenceId = msg.getChatId();

                boolean transmitReceipt = true;
                boolean handleSuccess = false;
                // determine what type of message we have and handle it accordingly
                try {
                    switch (msg.getType()) {
                        case FILE:
                            FileTransfer receiver = new FileTransferBuilder().setWithMi(withMi).makeFileTransfer();
                            receiver.receive(msg, incomingDir);
                            handleSuccess = true;
                            break;
                        case CHAT:
                            conferenceManager.handleConferenceMessage(customer.takeName(), msg.getTextMsg(), conferenceId);
                            handleSuccess = true;
                            break;
                        case CHAT_STATE:
                            String newConferenceName = msg.getTextMsg().getTextMsg();
                            conferenceManager.handleConferenceStateMessage(msg.getChatStateMsg(), conferenceId, newConferenceName);
                            handleSuccess = true;
                            break;
                        case READ_RECEIPT:
                            // don't send a receipt for a receipt
                            transmitReceipt = false;
                            handleSuccess = true;
                            break;
                    }
                } catch (Exception e) {
                    System.err.println("Error handling message: " + e.getMessage());
                }

                // reset the chat in case it has changed
                String conferenceName = conferenceManager.obtainConferenceNameFromUniqueId(conferenceId);
                Conversation conference = conferenceManager.pullConference(conferenceName);

                // we still want to send a receipt, even if we didn't handle it properly.
                if (transmitReceipt) {
                    Chat.WithMiMsg.Builder msgBuilder = MessageUtils.makeReceipt(handleSuccess, msg.getMessageId());
                    withMi.transmitMessage(msgBuilder, customer, conference);
                }
            } catch (Exception e) {
                System.err.println("Error parsing message: " + e.getMessage());
            }
            return null;
        }
    }

    private class ErrorGuide implements Callable<Void> {
        private final Exception deviation;

        private ErrorGuide(Exception deviation) {
            this.deviation = deviation;
        }

        @Override
        public Void call() throws Exception {
            throw deviation;
        }
    }
}
