package com.techtip.chatbox;

import com.techtip.control.Command;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;

/**
 * Prints the current chat name
 */
public class CurrentForumCommand extends Command {
    private static final String COMMAND = "currentchat";
    private final DropBy withMi;

    public CurrentForumCommand(DropBy withMi) {
        super(COMMAND, "Prints the name of the current chat");
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        Forum currentForum = withMi.fetchCurrentForum();
        out.println("current chat: " + currentForum.obtainName());

    }
}
