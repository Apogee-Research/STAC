package net.robotictip.dropbys;

import net.robotictip.display.Command;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;

/**
 * Prints the current chat name
 */
public class CurrentDiscussionCommand extends Command {
    private static final String COMMAND = "currentchat";
    private final HangIn withMi;

    public CurrentDiscussionCommand(HangIn withMi) {
        super(COMMAND, "Prints the name of the current chat");
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        Conversation currentDiscussion = withMi.grabCurrentDiscussion();
        out.println("current chat: " + currentDiscussion.takeName());

    }
}
