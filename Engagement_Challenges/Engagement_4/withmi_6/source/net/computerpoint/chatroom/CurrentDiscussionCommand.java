package net.computerpoint.chatroom;

import net.computerpoint.console.Command;
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
        WithMiChat currentDiscussion = withMi.takeCurrentDiscussion();
        out.println("current chat: " + currentDiscussion.grabName());

    }
}
