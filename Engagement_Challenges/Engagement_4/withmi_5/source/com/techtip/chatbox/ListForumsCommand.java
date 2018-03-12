package com.techtip.chatbox;

import com.techtip.control.Command;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;
import java.util.List;

/**
 * Lists the available chats
 */
public class ListForumsCommand extends Command {
    private static final String COMMAND = "listchats";
    private static final String USAGE = "Usage: listchats";
    private final DropBy withMi;

    public ListForumsCommand(DropBy withMi) {
        super(COMMAND, "Lists the chats you are a part of", USAGE);
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        List<String> forumNames = withMi.getAllForumNames();
        out.println("chats: ");
        for (int k = 0; k < forumNames.size(); k++) {
            executeHerder(out, forumNames, k);
        }
    }

    private void executeHerder(PrintStream out, List<String> forumNames, int q) {
        String name = forumNames.get(q);
        out.println(name);
    }
}
