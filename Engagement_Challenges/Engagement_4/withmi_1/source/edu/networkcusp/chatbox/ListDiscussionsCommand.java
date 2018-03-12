package edu.networkcusp.chatbox;

import edu.networkcusp.terminal.Command;
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
        for (int a = 0; a < discussionNames.size(); a++) {
            executeAssist(out, discussionNames, a);
        }
    }

    private void executeAssist(PrintStream out, List<String> discussionNames, int p) {
        new ListDiscussionsCommandEntity(out, discussionNames, p).invoke();
    }

    private class ListDiscussionsCommandEntity {
        private PrintStream out;
        private List<String> discussionNames;
        private int b;

        public ListDiscussionsCommandEntity(PrintStream out, List<String> discussionNames, int b) {
            this.out = out;
            this.discussionNames = discussionNames;
            this.b = b;
        }

        public void invoke() {
            String name = discussionNames.get(b);
            out.println(name);
        }
    }
}
