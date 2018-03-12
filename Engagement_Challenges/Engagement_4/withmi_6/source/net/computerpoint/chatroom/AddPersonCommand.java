package net.computerpoint.chatroom;

import net.computerpoint.dialogs.ProtocolsDeviation;
import net.computerpoint.console.Command;
import jline.console.completer.AggregateCompleter;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.StringsCompleter;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;
import java.util.List;

/**
 * Adds the specified user to the current chat
 */
public class AddPersonCommand extends Command {
    private static final String COMMAND = "adduser";
    private static final String USAGE = "Usage: adduser <user name>";
    private final HangIn withMi;

    public AddPersonCommand(HangIn withMi) {
        super(COMMAND, "Adds a known user to the current group chat", USAGE, new AggregateCompleter(new ArgumentCompleter(
                new StringsCompleter(COMMAND), new PersonCompleter(withMi))));
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        List<String> argList = cmd.getArgList();
        if (argList.size() != 1) {
            executeEntity(out);
        } else {
            executeFunction(out, argList);
        }
    }

    private void executeFunction(PrintStream out, List<String> argList) {
        try {
            WithMiChat currentDiscussion = withMi.takeCurrentDiscussion();

            if (currentDiscussion.canAddMorePersons()) {
                // get a known user
                String personName = argList.get(0);
                Participant person = withMi.obtainPerson(personName);

                if (person == null) {
                    withMi.printPersonMsg("No user is known with the name " + personName);
                    return;
                }

                if (!withMi.grabCurrentPersons().contains(person)) {
                    withMi.printPersonMsg("Not connected to user " + personName);
                    return;
                }

                // add the user to the chat
                withMi.addPersonToDiscussion(currentDiscussion, person);

                // create a state change builder
                Chat.WithMiMsg.Builder withMiMsgBuilder = MessageUtils.formDiscussionStateMsgBuilder(withMi.getMyIdentity(),
                        currentDiscussion);

                // send it to everyone in the chat, including the new user
                withMi.deliverMessage(withMiMsgBuilder, currentDiscussion);

                out.println("Added user to group");

            } else {
                executeFunctionExecutor(out, currentDiscussion);
            }
        } catch (ProtocolsDeviation e) {
            out.println("Failed to connect");
        }
    }

    private void executeFunctionExecutor(PrintStream out, WithMiChat currentDiscussion) {
        out.println("You cannot add another user to " + currentDiscussion.grabName());
    }

    private void executeEntity(PrintStream out) {
        out.println(USAGE);
    }
}
