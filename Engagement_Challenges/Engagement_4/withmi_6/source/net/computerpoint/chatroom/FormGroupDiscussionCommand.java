package net.computerpoint.chatroom;

import net.computerpoint.console.Command;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;
import java.util.List;

/**
 * Creates an empty group chat
 */
public class FormGroupDiscussionCommand extends Command {

    private static final String COMMAND = "createchat";
    private static final String USAGE = "Usage: createchat <name>";
    private final HangIn withMi;

    public FormGroupDiscussionCommand(HangIn withMi) {
        super(COMMAND, "Creates a group chat with the specified name", USAGE);
        this.withMi = withMi;
    }


    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        List<String> argList = cmd.getArgList();
        if (argList.size() != 1) {
            executeGuide(out);
        } else {
            executeWorker(out, argList);
        }

    }

    private void executeWorker(PrintStream out, List<String> argList) {
        String discussionName = argList.get(0);
        // The chat name cannot be too long or it may ruin the chat state side channel
        if (discussionName.length() > WithMiChat.MAX_NAME_LENGTH) {
            out.println("Chat names must be less than " + WithMiChat.MAX_NAME_LENGTH + " characters long.\n" +
            " Chat " + discussionName + " not created.");
            return;
        }

        if (withMi.formDiscussion(discussionName)) {
            executeWorkerGateKeeper(out, discussionName);
        } else {
            out.println("Unable to create group chat " + discussionName);
        }
    }

    private void executeWorkerGateKeeper(PrintStream out, String discussionName) {
        out.println("Successfully created " + discussionName);
    }

    private void executeGuide(PrintStream out) {
        out.println(USAGE);
    }
}
