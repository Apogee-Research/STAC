package net.computerpoint.chatroom;

import net.computerpoint.console.Command;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;
import java.util.List;

/**
 * Lists the available chats
 */
public class ListDiscussionsCommand extends Command {
    private static final String COMMAND = "listchats";
    private static final String USAGE = "Usage: listchats";
    private final HangIn withMi;

    public ListDiscussionsCommand(HangIn withMi) {
        super(COMMAND, "Lists the chats you are a part of", USAGE);
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        List<String> discussionNames = withMi.fetchAllDiscussionNames();
        out.println("chats: ");
        for (int j = 0; j < discussionNames.size(); j++) {
            executeEngine(out, discussionNames, j);
        }
    }

    private void executeEngine(PrintStream out, List<String> discussionNames, int i) {
        String name = discussionNames.get(i);
        out.println(name);
    }
}
