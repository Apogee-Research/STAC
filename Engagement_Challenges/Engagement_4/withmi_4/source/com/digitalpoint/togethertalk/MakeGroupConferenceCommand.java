package com.digitalpoint.togethertalk;

import com.digitalpoint.terminal.Command;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;
import java.util.List;

/**
 * Creates an empty group chat
 */
public class MakeGroupConferenceCommand extends Command {

    private static final String COMMAND = "createchat";
    private static final String USAGE = "Usage: createchat <name>";
    private final HangIn withMi;

    public MakeGroupConferenceCommand(HangIn withMi) {
        super(COMMAND, "Creates a group chat with the specified name", USAGE);
        this.withMi = withMi;
    }


    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        List<String> argList = cmd.getArgList();
        if (argList.size() != 1) {
            executeService(out);
        } else {
            String conferenceName = argList.get(0);
            // The chat name cannot be too long or it may ruin the chat state side channel
            if (conferenceName.length() > Forum.MAX_NAME_LENGTH) {
                out.println("Chat names must be less than " + Forum.MAX_NAME_LENGTH + " characters long.\n" +
                " Chat " + conferenceName + " not created.");
                return;
            }

            if (withMi.makeConference(conferenceName)) {
                executeEngine(out, conferenceName);
            } else {
                out.println("Unable to create group chat " + conferenceName);
            }
        }

    }

    private void executeEngine(PrintStream out, String conferenceName) {
        out.println("Successfully created " + conferenceName);
    }

    private void executeService(PrintStream out) {
        out.println(USAGE);
    }
}
