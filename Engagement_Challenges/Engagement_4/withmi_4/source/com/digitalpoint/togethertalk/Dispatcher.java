package com.digitalpoint.togethertalk;

import com.digitalpoint.dialogs.SenderReceiversConnection;
import com.digitalpoint.dialogs.SenderReceiversException;
import com.digitalpoint.terminal.Console;

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
    private final Console command;

    private final Queue<FutureTask<?>> futureJobQueue;
    private final ExecutorService executorService;

    public Dispatcher(HangIn withMi, ConversationManager conferenceManager, File incomingDir, Console command) {
        this.withMi = withMi;
        this.conferenceManager = conferenceManager;
        this.incomingDir = incomingDir;
        this.command = command;

        futureJobQueue = new ConcurrentLinkedQueue<>();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void handleMessage(Participant member, byte[] message) throws SenderReceiversException {
        futureJobQueue.add(new FutureTask<>(new MessageCoach(member, message)));
    }

    public void handleCommand(String line) {
        try {
            FutureTask<?> futureJob = new FutureTask<>(new CommandCoach(line));
            futureJobQueue.add(futureJob);
            // Need to wait for this command (and any pending messages)
            // to get executed before allowing another command
            futureJob.get();
        } catch (ExecutionException e) {
            handleError(new SenderReceiversException(e.getCause()));
        } catch (Exception e) {
            handleError(e);
        }
    }

    public void handleNewConnection(SenderReceiversConnection connection) {
        futureJobQueue.add(new FutureTask<>(new NewConnectionCoach(connection)));
    }

    public void handleClosedConnection(SenderReceiversConnection connection) {
        futureJobQueue.add(new FutureTask<>(new ClosedConnectionCoach(connection)));
    }

    public void handleError(Exception exception) {
        futureJobQueue.add(new FutureTask<>(new ErrorCoach(exception)));
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
        executorService.execute(new CommandRunner());

        // While waiting for the console to conclude,
        // process the results of all background dispatches in the
        // main thread so the user can be notified of any issues.
        while (!command.shouldExit()) {
            FutureTask<?> futureJob;
            while ((futureJob = futureJobQueue.poll()) != null) {
                runGateKeeper(futureJob);
            }
        }
    }

    private void runGateKeeper(FutureTask<?> futureJob) throws Throwable {
        try {
            futureJob.run();
            futureJob.get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof Exception) {
                runGateKeeperCoordinator(e);
            } else {
                runGateKeeperAssist(e);
            }
        }
    }

    private void runGateKeeperAssist(ExecutionException e) throws Throwable {
        throw e.getCause();
    }

    private void runGateKeeperCoordinator(ExecutionException e) throws SenderReceiversException {
        new WithMiDispatcherHome(e).invoke();
    }

    /**
     * Handles any remaining console messages, then shuts down
     */
    public void shutdown() {
        command.defineShouldExit(true);
        executorService.shutdown();
    }

    private class CommandRunner implements Runnable {
        @Override
        public void run() {
            while (!command.shouldExit()) {
                try {
                    handleCommand(command.grabNextCommand());
                } catch (Exception e) {
                    handleError(e);
                }
            }
        }
    }

    private class ClosedConnectionCoach implements Callable<Void> {
        private final SenderReceiversConnection connection;

        public ClosedConnectionCoach(SenderReceiversConnection connection) {
            this.connection = connection;
        }

        @Override
        public Void call() {
            try {
                withMi.removeConnection(connection);
            } catch (SenderReceiversException e) {
                System.err.println("Error handling closed connection " + e.getMessage());
            }
            return null;
        }
    }

    private class NewConnectionCoach implements Callable<Void> {
        private final SenderReceiversConnection connection;

        public NewConnectionCoach(SenderReceiversConnection connection) {
            this.connection = connection;
        }

        @Override
        public Void call() {
            try {
                withMi.handleConnection(connection, false);
            } catch (SenderReceiversException e) {
                System.err.println("Error handling new connection " + e.getMessage());
            }
            return null;
        }
    }

    /**
     * Handles the commands that we receive
     */
    private class CommandCoach implements Callable<Void> {
        private final String command;

        public CommandCoach(String command) {
            this.command = command;
        }

        @Override
        public Void call() {
            try {
                Dispatcher.this.command.executeCommand(command);
            } catch (IOException e) {
                System.err.println("Error executing command: " + e.getMessage());
            }
            return null;
        }
    }

    /**
     * Handles the messages that we receive
     */
    private class MessageCoach implements Callable<Void> {
        private final Participant member;
        private final byte[] data;

        public MessageCoach(Participant member, byte[] data) {
            this.member = member;
            this.data = data;
        }

        @Override
        public Void call() {
            try {
                // parse the message
                Chat.WithMiMsg msg = Chat.WithMiMsg.parseFrom(data);
                String conferenceId = msg.getChatId();

                boolean sendReceipt = true;
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
                            conferenceManager.handleConferenceMessage(member.grabName(), msg.getTextMsg(), conferenceId);
                            handleSuccess = true;
                            break;
                        case CHAT_STATE:
                            String newConferenceName = msg.getTextMsg().getTextMsg();
                            conferenceManager.handleConferenceStateMessage(msg.getChatStateMsg(), conferenceId, newConferenceName);
                            handleSuccess = true;
                            break;
                        case READ_RECEIPT:
                            // don't send a receipt for a receipt
                            sendReceipt = false;
                            handleSuccess = true;
                            break;
                    }
                } catch (Exception e) {
                    System.err.println("Error handling message: " + e.getMessage());
                }

                // reset the chat in case it has changed
                String conferenceName = conferenceManager.fetchConferenceNameFromUniqueId(conferenceId);
                Forum conference = conferenceManager.obtainConference(conferenceName);

                // we still want to send a receipt, even if we didn't handle it properly.
                if (sendReceipt) {
                    callHelp(msg, handleSuccess, conference);
                }
            } catch (Exception e) {
                System.err.println("Error parsing message: " + e.getMessage());
            }
            return null;
        }

        private void callHelp(Chat.WithMiMsg msg, boolean handleSuccess, Forum conference) throws SenderReceiversException {
            Chat.WithMiMsg.Builder msgBuilder = MessageUtils.makeReceipt(handleSuccess, msg.getMessageId());
            withMi.sendMessage(msgBuilder, member, conference);
        }
    }

    private class ErrorCoach implements Callable<Void> {
        private final Exception exception;

        private ErrorCoach(Exception exception) {
            this.exception = exception;
        }

        @Override
        public Void call() throws Exception {
            throw exception;
        }
    }

    private class WithMiDispatcherHome {
        private ExecutionException e;

        public WithMiDispatcherHome(ExecutionException e) {
            this.e = e;
        }

        public void invoke() throws SenderReceiversException {
            throw new SenderReceiversException(e.getCause());
        }
    }
}
