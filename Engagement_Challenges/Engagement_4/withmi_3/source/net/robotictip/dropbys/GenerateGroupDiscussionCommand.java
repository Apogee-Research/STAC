package net.robotictip.dropbys;

import net.robotictip.display.Command;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;
import java.util.List;

/**
 * Creates an empty group chat
 */
public class GenerateGroupDiscussionCommand extends Command {

    private static final String COMMAND = "createchat";
    private static final String USAGE = "Usage: createchat <name>";
    private final HangIn withMi;

    public GenerateGroupDiscussionCommand(HangIn withMi) {
        super(COMMAND, "Creates a group chat with the specified name", USAGE);
        this.withMi = withMi;
    }


    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        List<String> argList = cmd.getArgList();
        if (argList.size() != 1) {
            out.println(USAGE);
        } else {
            executeFunction(out, argList);
        }

    }

    private void executeFunction(PrintStream out, List<String> argList) {
        String discussionName = argList.get(0);
        // The chat name cannot be too long or it may ruin the chat state side channel
        if (discussionName.length() > Conversation.MAX_NAME_LENGTH) {
            executeFunctionHerder(out, discussionName);
            return;
        }

        if (withMi.generateDiscussion(discussionName)) {
            out.println("Successfully created " + discussionName);
        } else {
            out.println("Unable to create group chat " + discussionName);
        }
    }

    private void executeFunctionHerder(PrintStream out, String discussionName) {
        out.println("Chat names must be less than " + Conversation.MAX_NAME_LENGTH + " characters long.\n" +
        " Chat " + discussionName + " not created.");
        return;
    }
}
