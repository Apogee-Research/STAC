package net.robotictip.dropbys;

import net.robotictip.protocols.SenderReceiversConnection;
import net.robotictip.protocols.SenderReceiversTrouble;
import net.robotictip.display.Display;

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
    private final ConversationManager discussionManager;
    private final File incomingDir;
    private final Display display;

    private final Queue<FutureTask<?>> futureTaskQueue;
    private final ExecutorService executorService;

    public Mediator(HangIn withMi, ConversationManager discussionManager, File incomingDir, Display display) {
        this.withMi = withMi;
        this.discussionManager = discussionManager;
        this.incomingDir = incomingDir;
        this.display = display;

        futureTaskQueue = new ConcurrentLinkedQueue<>();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void handleMessage(Chatee user, byte[] message) throws SenderReceiversTrouble {
        futureTaskQueue.add(new FutureTask<>(new MessageManager(user, message)));
    }

    public void handleCommand(String line) {
        try {
            FutureTask<?> futureTask = new FutureTask<>(new CommandManager(line));
            futureTaskQueue.add(futureTask);
            // Need to wait for this command (and any pending messages)
            // to get executed before allowing another command
            futureTask.get();
        } catch (ExecutionException e) {
            handleError(new SenderReceiversTrouble(e.getCause()));
        } catch (Exception e) {
            handleError(e);
        }
    }

    public void handleNewConnection(SenderReceiversConnection connection) {
        futureTaskQueue.add(new FutureTask<>(new NewConnectionManager(connection)));
    }

    public void handleClosedConnection(SenderReceiversConnection connection) {
        futureTaskQueue.add(new FutureTask<>(new ClosedConnectionManager(connection)));
    }

    public void handleError(Exception trouble) {
        futureTaskQueue.add(new FutureTask<>(new ErrorManager(trouble)));
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
                runExecutor(futureTask);
            }
        }
    }

    private void runExecutor(FutureTask<?> futureTask) throws Throwable {
        try {
            futureTask.run();
            futureTask.get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof Exception) {
                runExecutorHome(e);
            } else {
                throw e.getCause();
            }
        }
    }

    private void runExecutorHome(ExecutionException e) throws SenderReceiversTrouble {
        throw new SenderReceiversTrouble(e.getCause());
    }

    /**
     * Handles any remaining console messages, then shuts down
     */
    public void shutdown() {
        display.defineShouldExit(true);
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

    private class ClosedConnectionManager implements Callable<Void> {
        private final SenderReceiversConnection connection;

        public ClosedConnectionManager(SenderReceiversConnection connection) {
            this.connection = connection;
        }

        @Override
        public Void call() {
            try {
                withMi.removeConnection(connection);
            } catch (SenderReceiversTrouble e) {
                System.err.println("Error handling closed connection " + e.getMessage());
            }
            return null;
        }
    }

    private class NewConnectionManager implements Callable<Void> {
        private final SenderReceiversConnection connection;

        public NewConnectionManager(SenderReceiversConnection connection) {
            this.connection = connection;
        }

        @Override
        public Void call() {
            try {
                withMi.handleConnection(connection, false);
            } catch (SenderReceiversTrouble e) {
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
    private class MessageManager implements Callable<Void> {
        private final Chatee user;
        private final byte[] data;

        public MessageManager(Chatee user, byte[] data) {
            this.user = user;
            this.data = data;
        }

        @Override
        public Void call() {
            try {
                // parse the message
                Chat.WithMiMsg msg = Chat.WithMiMsg.parseFrom(data);
                String discussionId = msg.getChatId();

                boolean transferReceipt = true;
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
                            discussionManager.handleDiscussionMessage(user.obtainName(), msg.getTextMsg(), discussionId);
                            handleSuccess = true;
                            break;
                        case CHAT_STATE:
                            String newDiscussionName = msg.getTextMsg().getTextMsg();
                            discussionManager.handleDiscussionStateMessage(msg.getChatStateMsg(), discussionId, newDiscussionName);
                            handleSuccess = true;
                            break;
                        case READ_RECEIPT:
                            // don't send a receipt for a receipt
                            transferReceipt = false;
                            handleSuccess = true;
                            break;
                    }
                } catch (Exception e) {
                    System.err.println("Error handling message: " + e.getMessage());
                }

                // reset the chat in case it has changed
                String discussionName = discussionManager.takeDiscussionNameFromUniqueId(discussionId);
                Conversation discussion = discussionManager.getDiscussion(discussionName);

                // we still want to send a receipt, even if we didn't handle it properly.
                if (transferReceipt) {
                    callSupervisor(msg, handleSuccess, discussion);
                }
            } catch (Exception e) {
                System.err.println("Error parsing message: " + e.getMessage());
            }
            return null;
        }

        private void callSupervisor(Chat.WithMiMsg msg, boolean handleSuccess, Conversation discussion) throws SenderReceiversTrouble {
            Chat.WithMiMsg.Builder msgBuilder = MessageUtils.generateReceipt(handleSuccess, msg.getMessageId());
            withMi.transferMessage(msgBuilder, user, discussion);
        }
    }

    private class ErrorManager implements Callable<Void> {
        private final Exception trouble;

        private ErrorManager(Exception trouble) {
            this.trouble = trouble;
        }

        @Override
        public Void call() throws Exception {
            throw trouble;
        }
    }
}
