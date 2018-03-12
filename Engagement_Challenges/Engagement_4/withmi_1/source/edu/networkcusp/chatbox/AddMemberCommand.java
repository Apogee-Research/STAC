package edu.networkcusp.chatbox;

import edu.networkcusp.protocols.CommunicationsFailure;
import edu.networkcusp.terminal.Command;
import jline.console.completer.AggregateCompleter;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.StringsCompleter;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;
import java.util.List;

/**
 * Adds the specified user to the current chat
 */
public class AddMemberCommand extends Command {
    private static final String COMMAND = "adduser";
    private static final String USAGE = "Usage: adduser <user name>";
    private final HangIn withMi;

    public AddMemberCommand(HangIn withMi) {
        super(COMMAND, "Adds a known user to the current group chat", USAGE, new AggregateCompleter(new ArgumentCompleter(
                new StringsCompleter(COMMAND), new MemberCompleter(withMi))));
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        List<String> argList = cmd.getArgList();
        if (argList.size() != 1) {
            executeHome(out);
        } else {
            try {
                WithMiChat currentDiscussion = withMi.obtainCurrentDiscussion();

                if (currentDiscussion.canAddMoreMembers()) {
                    // get a known user
                    String memberName = argList.get(0);
                    WithMiUser member = withMi.fetchMember(memberName);

                    if (member == null) {
                        withMi.printMemberMsg("No user is known with the name " + memberName);
                        return;
                    }

                    if (!withMi.grabCurrentMembers().contains(member)) {
                        withMi.printMemberMsg("Not connected to user " + memberName);
                        return;
                    }

                    // add the user to the chat
                    withMi.addMemberToDiscussion(currentDiscussion, member);

                    // create a state change builder
                    Chat.WithMiMsg.Builder withMiMsgBuilder = withMi.takeMyIdentity().createDiscussionStateMsgBuilder(
                            currentDiscussion);

                    // send it to everyone in the chat, including the new user
                    withMi.deliverMessage(withMiMsgBuilder, currentDiscussion);

                    out.println("Added user to group");

                } else {
                    out.println("You cannot add another user to " + currentDiscussion.grabName());
                }
            } catch (CommunicationsFailure e) {
                out.println("Failed to connect");
            }
        }
    }

    private void executeHome(PrintStream out) {
        new AddMemberCommandGuide(out).invoke();
    }

    private class AddMemberCommandGuide {
        private PrintStream out;

        public AddMemberCommandGuide(PrintStream out) {
            this.out = out;
        }

        public void invoke() {
            out.println(USAGE);
        }
    }
}
