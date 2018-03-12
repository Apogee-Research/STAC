package com.digitalpoint.togethertalk;

import com.digitalpoint.dialogs.SenderReceiversException;
import com.digitalpoint.terminal.Command;
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
                new StringsCompleter(COMMAND), new MemberCompleterBuilder().assignWithMi(withMi).makeMemberCompleter())));
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        List<String> argList = cmd.getArgList();
        if (argList.size() != 1) {
            out.println(USAGE);
        } else {
            try {
                Forum currentConference = withMi.obtainCurrentConference();

                if (currentConference.canAddMoreMembers()) {
                    // get a known user
                    String memberName = argList.get(0);
                    Participant member = withMi.getMember(memberName);

                    if (member == null) {
                        withMi.printMemberMsg("No user is known with the name " + memberName);
                        return;
                    }

                    if (!withMi.getCurrentMembers().contains(member)) {
                        executeService(memberName);
                        return;
                    }

                    // add the user to the chat
                    withMi.addMemberToConference(currentConference, member);

                    // create a state change builder
                    Chat.WithMiMsg.Builder withMiMsgBuilder = MessageUtils.makeConferenceStateMsgBuilder(withMi.getMyIdentity(),
                            currentConference);

                    // send it to everyone in the chat, including the new user
                    withMi.sendMessage(withMiMsgBuilder, currentConference);

                    out.println("Added user to group");

                } else {
                    executeTarget(out, currentConference);
                }
            } catch (SenderReceiversException e) {
                out.println("Failed to connect");
            }
        }
    }

    private void executeTarget(PrintStream out, Forum currentConference) {
        out.println("You cannot add another user to " + currentConference.pullName());
    }

    private void executeService(String memberName) {
        new AddMemberCommandFunction(memberName).invoke();
        return;
    }

    private class AddMemberCommandFunction {
        private String memberName;

        public AddMemberCommandFunction(String memberName) {
            this.memberName = memberName;
        }

        public void invoke() {
            withMi.printMemberMsg("Not connected to user " + memberName);
            return;
        }
    }
}
