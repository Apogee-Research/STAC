package org.digitaltip.chatroom;

import org.digitaltip.ui.Command;
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
                new StringsCompleter(COMMAND), new JoinConferenceCompleter(withMi))));
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        List<String> argList = cmd.getArgList();
        if (argList.size() != 1) {
            out.println(USAGE);
            out.println("The command 'joinchat' allows you to switch to an existing chat");
        } else {
            String conferenceName = argList.get(0);

            // check that the chat exists
            List<String> conferenceNames = withMi.grabAllConferenceNames();
            if (conferenceNames.contains(conferenceName)) {
                // join chat associated with given name
                Conversation conference = withMi.switchToConference(conferenceName);
                out.println("joined " + conferenceName);

                // print all unread messages
                List<String> unreadMessages = conference.readUnreadMessages();
                if (unreadMessages.isEmpty()) {
                    executeService(out);
                } else {
                    out.println("unread messages: ");
                    for (int j = 0; j < unreadMessages.size(); j++) {
                        String message = unreadMessages.get(j);
                        out.println(message);
                    }
                }
            } else {
                out.println("No chat exists with the name " + conferenceName);
            }
        }
    }

    private void executeService(PrintStream out) {
        out.println("no unread messsages");
    }
}
