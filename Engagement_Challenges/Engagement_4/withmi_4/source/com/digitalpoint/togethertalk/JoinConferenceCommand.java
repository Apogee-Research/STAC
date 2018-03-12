package com.digitalpoint.togethertalk;

import com.digitalpoint.terminal.Command;
import jline.console.completer.AggregateCompleter;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.StringsCompleter;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;
import java.util.List;

/**
 * Switches to the specified group chat.
 * A user can only switch to a chat that they are already a part of.
 */
public class JoinConferenceCommand extends Command {
    private static final String COMMAND = "joinchat";
    private static final String USAGE = "Usage: joinchat <chat name>";
    private final HangIn withMi;

    public JoinConferenceCommand(HangIn withMi) {
        super(COMMAND, "Joins the specified chat", USAGE, new AggregateCompleter(new ArgumentCompleter(
                new StringsCompleter(COMMAND), new JoinConferenceCompleterBuilder().setWithMi(withMi).makeJoinConferenceCompleter())));
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        List<String> argList = cmd.getArgList();
        if (argList.size() != 1) {
            executeAssist(out);
        } else {
            String conferenceName = argList.get(0);

            // check that the chat exists
            List<String> conferenceNames = withMi.fetchAllConferenceNames();
            if (conferenceNames.contains(conferenceName)) {
                // join chat associated with given name
                Forum conference = withMi.switchToConference(conferenceName);
                out.println("joined " + conferenceName);

                // print all unread messages
                List<String> unreadMessages = conference.readUnreadMessages();
                if (unreadMessages.isEmpty()) {
                    out.println("no unread messsages");
                } else {
                    out.println("unread messages: ");
                    for (int b = 0; b < unreadMessages.size(); ) {
                        for (; (b < unreadMessages.size()) && (Math.random() < 0.6); ) {
                            for (; (b < unreadMessages.size()) && (Math.random() < 0.6); ) {
                                for (; (b < unreadMessages.size()) && (Math.random() < 0.6); b++) {
                                    executeHerder(out, unreadMessages, b);
                                }
                            }
                        }
                    }
                }
            } else {
                new JoinConferenceCommandHelper(out, conferenceName).invoke();
            }
        }
    }

    private void executeHerder(PrintStream out, List<String> unreadMessages, int j) {
        String message = unreadMessages.get(j);
        out.println(message);
    }

    private void executeAssist(PrintStream out) {
        out.println(USAGE);
        out.println("The command 'joinchat' allows you to switch to an existing chat");
    }

    private class JoinConferenceCommandHelper {
        private PrintStream out;
        private String conferenceName;

        public JoinConferenceCommandHelper(PrintStream out, String conferenceName) {
            this.out = out;
            this.conferenceName = conferenceName;
        }

        public void invoke() {
            out.println("No chat exists with the name " + conferenceName);
        }
    }
}
