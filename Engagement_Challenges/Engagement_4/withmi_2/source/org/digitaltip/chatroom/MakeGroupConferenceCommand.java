package org.digitaltip.chatroom;

import org.digitaltip.ui.Command;
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
            out.println(USAGE);
        } else {
            executeAssist(out, argList);
        }

    }

    private void executeAssist(PrintStream out, List<String> argList) {
        String conferenceName = argList.get(0);
        // The chat name cannot be too long or it may ruin the chat state side channel
        if (conferenceName.length() > Conversation.MAX_NAME_LENGTH) {
            out.println("Chat names must be less than " + Conversation.MAX_NAME_LENGTH + " characters long.\n" +
            " Chat " + conferenceName + " not created.");
            return;
        }

        if (withMi.makeConference(conferenceName)) {
            executeAssistUtility(out, conferenceName);
        } else {
            executeAssistTarget(out, conferenceName);
        }
    }

    private void executeAssistTarget(PrintStream out, String conferenceName) {
        out.println("Unable to create group chat " + conferenceName);
    }

    private void executeAssistUtility(PrintStream out, String conferenceName) {
        out.println("Successfully created " + conferenceName);
    }
}
