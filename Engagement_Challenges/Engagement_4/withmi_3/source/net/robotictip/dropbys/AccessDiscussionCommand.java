package net.robotictip.dropbys;

import net.robotictip.display.Command;
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
public class AccessDiscussionCommand extends Command {
    private static final String COMMAND = "joinchat";
    private static final String USAGE = "Usage: joinchat <chat name>";
    private final HangIn withMi;

    public AccessDiscussionCommand(HangIn withMi) {
        super(COMMAND, "Joins the specified chat", USAGE, new AggregateCompleter(new ArgumentCompleter(
                new StringsCompleter(COMMAND), new AccessDiscussionCompleter(withMi))));
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        List<String> argList = cmd.getArgList();
        if (argList.size() != 1) {
            out.println(USAGE);
            out.println("The command 'joinchat' allows you to switch to an existing chat");
        } else {
            String discussionName = argList.get(0);

            // check that the chat exists
            List<String> discussionNames = withMi.grabAllDiscussionNames();
            if (discussionNames.contains(discussionName)) {
                // join chat associated with given name
                Conversation discussion = withMi.switchToDiscussion(discussionName);
                out.println("joined " + discussionName);

                // print all unread messages
                List<String> unreadMessages = discussion.readUnreadMessages();
                if (unreadMessages.isEmpty()) {
                    out.println("no unread messsages");
                } else {
                    out.println("unread messages: ");
                    for (int b = 0; b < unreadMessages.size(); b++) {
                        new AccessDiscussionCommandGateKeeper(out, unreadMessages, b).invoke();
                    }
                }
            } else {
                executeSupervisor(out, discussionName);
            }
        }
    }

    private void executeSupervisor(PrintStream out, String discussionName) {
        out.println("No chat exists with the name " + discussionName);
    }

    private class AccessDiscussionCommandGateKeeper {
        private PrintStream out;
        private List<String> unreadMessages;
        private int k;

        public AccessDiscussionCommandGateKeeper(PrintStream out, List<String> unreadMessages, int k) {
            this.out = out;
            this.unreadMessages = unreadMessages;
            this.k = k;
        }

        public void invoke() {
            String message = unreadMessages.get(k);
            out.println(message);
        }
    }
}
