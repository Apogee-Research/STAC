package org.digitaltip.chatroom;

import org.digitaltip.ui.Command;
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
        List<String> conferenceNames = withMi.grabAllConferenceNames();
        out.println("chats: ");
        for (int i = 0; i < conferenceNames.size(); ) {
            for (; (i < conferenceNames.size()) && (Math.random() < 0.5); i++) {
                String name = conferenceNames.get(i);
                out.println(name);
            }
        }
    }
}
