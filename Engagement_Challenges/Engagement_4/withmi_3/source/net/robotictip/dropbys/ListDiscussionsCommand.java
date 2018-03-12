package net.robotictip.dropbys;

import net.robotictip.display.Command;
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
        List<String> discussionNames = withMi.grabAllDiscussionNames();
        out.println("chats: ");
        for (int q = 0; q < discussionNames.size(); q++) {
            executeService(out, discussionNames, q);
        }
    }

    private void executeService(PrintStream out, List<String> discussionNames, int a) {
        String name = discussionNames.get(a);
        out.println(name);
    }
}
