package com.techtip.chatbox;

import com.techtip.control.Command;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;
import java.util.List;

/**
 * Creates an empty group chat
 */
public class FormGroupForumCommand extends Command {

    private static final String COMMAND = "createchat";
    private static final String USAGE = "Usage: createchat <name>";
    private final DropBy withMi;

    public FormGroupForumCommand(DropBy withMi) {
        super(COMMAND, "Creates a group chat with the specified name", USAGE);
        this.withMi = withMi;
    }


    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        List<String> argList = cmd.getArgList();
        if (argList.size() != 1) {
            out.println(USAGE);
        } else {
            executeUtility(out, argList);
        }

    }

    private void executeUtility(PrintStream out, List<String> argList) {
        String forumName = argList.get(0);
        // The chat name cannot be too long or it may ruin the chat state side channel
        if (forumName.length() > Forum.MAX_NAME_LENGTH) {
            executeUtilityCoordinator(out, forumName);
            return;
        }

        if (withMi.formForum(forumName)) {
            new FormGroupForumCommandAdviser(out, forumName).invoke();
        } else {
            executeUtilityHerder(out, forumName);
        }
    }

    private void executeUtilityHerder(PrintStream out, String forumName) {
        out.println("Unable to create group chat " + forumName);
    }

    private void executeUtilityCoordinator(PrintStream out, String forumName) {
        out.println("Chat names must be less than " + Forum.MAX_NAME_LENGTH + " characters long.\n" +
        " Chat " + forumName + " not created.");
        return;
    }

    private class FormGroupForumCommandAdviser {
        private PrintStream out;
        private String forumName;

        public FormGroupForumCommandAdviser(PrintStream out, String forumName) {
            this.out = out;
            this.forumName = forumName;
        }

        public void invoke() {
            out.println("Successfully created " + forumName);
        }
    }
}
