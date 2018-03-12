package com.digitalpoint.togethertalk;

import com.digitalpoint.terminal.Command;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;
import java.util.List;

/**
 * Lists the available chats
 */
public class ListConferencesCommand extends Command {
    private static final String COMMAND = "listchats";
    private static final String USAGE = "Usage: listchats";
    private final HangIn withMi;

    public ListConferencesCommand(HangIn withMi) {
        super(COMMAND, "Lists the chats you are a part of", USAGE);
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        List<String> conferenceNames = withMi.fetchAllConferenceNames();
        out.println("chats: ");
        for (int i = 0; i < conferenceNames.size(); i++) {
            executeAdviser(out, conferenceNames, i);
        }
    }

    private void executeAdviser(PrintStream out, List<String> conferenceNames, int b) {
        String name = conferenceNames.get(b);
        out.println(name);
    }
}
