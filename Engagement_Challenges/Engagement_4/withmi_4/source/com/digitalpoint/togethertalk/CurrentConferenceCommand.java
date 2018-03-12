package com.digitalpoint.togethertalk;

import com.digitalpoint.terminal.Command;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;

/**
 * Prints the current chat name
 */
public class CurrentConferenceCommand extends Command {
    private static final String COMMAND = "currentchat";
    private final HangIn withMi;

    public CurrentConferenceCommand(HangIn withMi) {
        super(COMMAND, "Prints the name of the current chat");
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        Forum currentConference = withMi.obtainCurrentConference();
        out.println("current chat: " + currentConference.pullName());

    }
}
