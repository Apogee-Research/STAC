package com.techtip.chatbox;

import com.techtip.communications.DialogsConnection;
import com.techtip.communications.DialogsDeviation;
import com.techtip.control.Ui;

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
    private final DropBy withMi;
    private final ConversationManager forumManager;
    private final File incomingDir;
    private final Ui ui;

    private final Queue<FutureTask<?>> futureTaxQueue;
    private final ExecutorService executorService;

    public Mediator(DropBy withMi, ConversationManager forumManager, File incomingDir, Ui ui) {
        this.withMi = withMi;
        this.forumManager = forumManager;
        this.incomingDir = incomingDir;
        this.ui = ui;

        futureTaxQueue = new ConcurrentLinkedQueue<>();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void handleMessage(WithMiUser customer, byte[] message) throws DialogsDeviation {
        futureTaxQueue.add(new FutureTask<>(new MessageHandler(customer, message)));
    }

    public void handleCommand(String line) {
        try {
            FutureTask<?> futureTax = new FutureTask<>(new CommandHandler(line));
            futureTaxQueue.add(futureTax);
            // Need to wait for this command (and any pending messages)
            // to get executed before allowing another command
            futureTax.get();
        } catch (ExecutionException e) {
            handleError(new DialogsDeviation(e.getCause()));
        } catch (Exception e) {
            handleError(e);
        }
    }

    public void handleNewConnection(DialogsConnection connection) {
        futureTaxQueue.add(new FutureTask<>(new NewConnectionHandler(connection)));
    }

    public void handleClosedConnection(DialogsConnection connection) {
        futureTaxQueue.add(new FutureTask<>(new ClosedConnectionHandler(connection)));
    }

    public void handleError(Exception deviation) {
        futureTaxQueue.add(new FutureTask<>(new ErrorHandler(deviation)));
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
        executorService.execute(new UiRunner());

        // While waiting for the console to conclude,
        // process the results of all background dispatches in the
        // main thread so the user can be notified of any issues.
        while (!ui.shouldExit()) {
            FutureTask<?> futureTax;
            while ((futureTax = futureTaxQueue.poll()) != null) {
                runHerder(futureTax);
            }
        }
    }

    private void runHerder(FutureTask<?> futureTax) throws Throwable {
        try {
            futureTax.run();
            futureTax.get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof Exception) {
                throw new DialogsDeviation(e.getCause());
            } else {
                new WithMiDispatcherUtility(e).invoke();
            }
        }
    }

    /**
     * Handles any remaining console messages, then shuts down
     */
    public void shutdown() {
        ui.assignShouldExit(true);
        executorService.shutdown();
    }

    private class UiRunner implements Runnable {
        @Override
        public void run() {
            while (!ui.shouldExit()) {
                try {
                    handleCommand(ui.pullNextCommand());
                } catch (Exception e) {
                    handleError(e);
                }
            }
        }
    }

    private class ClosedConnectionHandler implements Callable<Void> {
        private final DialogsConnection connection;

        public ClosedConnectionHandler(DialogsConnection connection) {
            this.connection = connection;
        }

        @Override
        public Void call() {
            try {
                withMi.removeConnection(connection);
            } catch (DialogsDeviation e) {
                System.err.println("Error handling closed connection " + e.getMessage());
            }
            return null;
        }
    }

    private class NewConnectionHandler implements Callable<Void> {
        private final DialogsConnection connection;

        public NewConnectionHandler(DialogsConnection connection) {
            this.connection = connection;
        }

        @Override
        public Void call() {
            try {
                withMi.handleConnection(connection, false);
            } catch (DialogsDeviation e) {
                System.err.println("Error handling new connection " + e.getMessage());
            }
            return null;
        }
    }

    /**
     * Handles the commands that we receive
     */
    private class CommandHandler implements Callable<Void> {
        private final String command;

        public CommandHandler(String command) {
            this.command = command;
        }

        @Override
        public Void call() {
            try {
                ui.executeCommand(command);
            } catch (IOException e) {
                System.err.println("Error executing command: " + e.getMessage());
            }
            return null;
        }
    }

    /**
     * Handles the messages that we receive
     */
    private class MessageHandler implements Callable<Void> {
        private final WithMiUser customer;
        private final byte[] data;

        public MessageHandler(WithMiUser customer, byte[] data) {
            this.customer = customer;
            this.data = data;
        }

        @Override
        public Void call() {
            try {
                // parse the message
                Chat.WithMiMsg msg = Chat.WithMiMsg.parseFrom(data);
                String forumId = msg.getChatId();

                boolean transmitReceipt = true;
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
                            forumManager.handleForumMessage(customer.pullName(), msg.getTextMsg(), forumId);
                            handleSuccess = true;
                            break;
                        case CHAT_STATE:
                            String newForumName = msg.getTextMsg().getTextMsg();
                            forumManager.handleForumStateMessage(msg.getChatStateMsg(), forumId, newForumName);
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
                String forumName = forumManager.getForumNameFromUniqueId(forumId);
                Forum forum = forumManager.takeForum(forumName);

                // we still want to send a receipt, even if we didn't handle it properly.
                if (transmitReceipt) {
                    Chat.WithMiMsg.Builder msgBuilder = MessageUtils.formReceipt(handleSuccess, msg.getMessageId());
                    withMi.transmitMessage(msgBuilder, customer, forum);
                }
            } catch (Exception e) {
                System.err.println("Error parsing message: " + e.getMessage());
            }
            return null;
        }
    }

    private class ErrorHandler implements Callable<Void> {
        private final Exception deviation;

        private ErrorHandler(Exception deviation) {
            this.deviation = deviation;
        }

        @Override
        public Void call() throws Exception {
            throw deviation;
        }
    }

    private class WithMiDispatcherUtility {
        private ExecutionException e;

        public WithMiDispatcherUtility(ExecutionException e) {
            this.e = e;
        }

        public void invoke() throws Throwable {
            throw e.getCause();
        }
    }
}
