package com.techtip.chatbox;

import com.techtip.control.Command;
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
public class EnterForumCommand extends Command {
    private static final String COMMAND = "joinchat";
    private static final String USAGE = "Usage: joinchat <chat name>";
    private final DropBy withMi;

    public EnterForumCommand(DropBy withMi) {
        super(COMMAND, "Joins the specified chat", USAGE, new AggregateCompleter(new ArgumentCompleter(
                new StringsCompleter(COMMAND), new EnterForumCompleterBuilder().defineWithMi(withMi).formEnterForumCompleter())));
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        List<String> argList = cmd.getArgList();
        if (argList.size() != 1) {
            out.println(USAGE);
            out.println("The command 'joinchat' allows you to switch to an existing chat");
        } else {
            String forumName = argList.get(0);

            // check that the chat exists
            List<String> forumNames = withMi.getAllForumNames();
            if (forumNames.contains(forumName)) {
                // join chat associated with given name
                Forum forum = withMi.switchToForum(forumName);
                out.println("joined " + forumName);

                // print all unread messages
                List<String> unreadMessages = forum.readUnreadMessages();
                if (unreadMessages.isEmpty()) {
                    out.println("no unread messsages");
                } else {
                    out.println("unread messages: ");
                    for (int b = 0; b < unreadMessages.size(); b++) {
                        executeAssist(out, unreadMessages, b);
                    }
                }
            } else {
                out.println("No chat exists with the name " + forumName);
            }
        }
    }

    private void executeAssist(PrintStream out, List<String> unreadMessages, int b) {
        String message = unreadMessages.get(b);
        out.println(message);
    }
}
