package edu.networkcusp.chatbox;

import edu.networkcusp.terminal.Command;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;
import java.util.List;

/**
 * Creates an empty group chat
 */
public class CreateGroupDiscussionCommand extends Command {

    private static final String COMMAND = "createchat";
    private static final String USAGE = "Usage: createchat <name>";
    private final HangIn withMi;

    public CreateGroupDiscussionCommand(HangIn withMi) {
        super(COMMAND, "Creates a group chat with the specified name", USAGE);
        this.withMi = withMi;
    }


    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        List<String> argList = cmd.getArgList();
        if (argList.size() != 1) {
            executeAid(out);
        } else {
            String discussionName = argList.get(0);
            // The chat name cannot be too long or it may ruin the chat state side channel
            if (discussionName.length() > WithMiChat.MAX_NAME_LENGTH) {
                out.println("Chat names must be less than " + WithMiChat.MAX_NAME_LENGTH + " characters long.\n" +
                " Chat " + discussionName + " not created.");
                return;
            }

            if (withMi.createDiscussion(discussionName)) {
                executeHerder(out, discussionName);
            } else {
                executeTarget(out, discussionName);
            }
        }

    }

    private void executeTarget(PrintStream out, String discussionName) {
        new CreateGroupDiscussionCommandHome(out, discussionName).invoke();
    }

    private void executeHerder(PrintStream out, String discussionName) {
        out.println("Successfully created " + discussionName);
    }

    private void executeAid(PrintStream out) {
        out.println(USAGE);
    }

    private class CreateGroupDiscussionCommandHome {
        private PrintStream out;
        private String discussionName;

        public CreateGroupDiscussionCommandHome(PrintStream out, String discussionName) {
            this.out = out;
            this.discussionName = discussionName;
        }

        public void invoke() {
            out.println("Unable to create group chat " + discussionName);
        }
    }
}
