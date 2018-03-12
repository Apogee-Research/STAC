package org.digitaltip.chatroom;

import org.digitaltip.ui.Command;
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
        Conversation currentConference = withMi.pullCurrentConference();
        out.println("current chat: " + currentConference.getName());

    }
}
