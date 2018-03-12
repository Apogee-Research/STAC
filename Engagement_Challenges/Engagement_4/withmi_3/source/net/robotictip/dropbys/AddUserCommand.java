package net.robotictip.dropbys;

import net.robotictip.protocols.SenderReceiversTrouble;
import net.robotictip.display.Command;
import jline.console.completer.AggregateCompleter;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.StringsCompleter;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;
import java.util.List;

/**
 * Adds the specified user to the current chat
 */
public class AddUserCommand extends Command {
    private static final String COMMAND = "adduser";
    private static final String USAGE = "Usage: adduser <user name>";
    private final HangIn withMi;

    public AddUserCommand(HangIn withMi) {
        super(COMMAND, "Adds a known user to the current group chat", USAGE, new AggregateCompleter(new ArgumentCompleter(
                new StringsCompleter(COMMAND), new UserCompleterBuilder().setWithMi(withMi).generateUserCompleter())));
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        List<String> argList = cmd.getArgList();
        if (argList.size() != 1) {
            out.println(USAGE);
        } else {
            try {
                Conversation currentDiscussion = withMi.grabCurrentDiscussion();

                if (currentDiscussion.canAddMoreUsers()) {
                    // get a known user
                    executeAid(out, argList, currentDiscussion);

                } else {
                    executeWorker(out, currentDiscussion);
                }
            } catch (SenderReceiversTrouble e) {
                out.println("Failed to connect");
            }
        }
    }

    private void executeWorker(PrintStream out, Conversation currentDiscussion) {
        new AddUserCommandWorker(out, currentDiscussion).invoke();
    }

    private void executeAid(PrintStream out, List<String> argList, Conversation currentDiscussion) throws SenderReceiversTrouble {
        String userName = argList.get(0);
        Chatee user = withMi.pullUser(userName);

        if (user == null) {
            new AddUserCommandUtility(userName).invoke();
            return;
        }

        if (!withMi.getCurrentUsers().contains(user)) {
            withMi.printUserMsg("Not connected to user " + userName);
            return;
        }

        // add the user to the chat
        withMi.addUserToDiscussion(currentDiscussion, user);

        // create a state change builder
        Chat.WithMiMsg.Builder withMiMsgBuilder = withMi.fetchMyIdentity().generateDiscussionStateMsgBuilder(
                currentDiscussion);

        // send it to everyone in the chat, including the new user
        withMi.transferMessage(withMiMsgBuilder, currentDiscussion);

        out.println("Added user to group");
    }

    private class AddUserCommandUtility {
        private String userName;

        public AddUserCommandUtility(String userName) {
            this.userName = userName;
        }

        public void invoke() {
            withMi.printUserMsg("No user is known with the name " + userName);
            return;
        }
    }

    private class AddUserCommandWorker {
        private PrintStream out;
        private Conversation currentDiscussion;

        public AddUserCommandWorker(PrintStream out, Conversation currentDiscussion) {
            this.out = out;
            this.currentDiscussion = currentDiscussion;
        }

        public void invoke() {
            out.println("You cannot add another user to " + currentDiscussion.takeName());
        }
    }
}
