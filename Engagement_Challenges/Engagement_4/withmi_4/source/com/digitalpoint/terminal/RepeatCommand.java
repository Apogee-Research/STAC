package com.digitalpoint.terminal;

import org.apache.commons.cli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class RepeatCommand extends Command {
    private static final String NAME = "repeat";
    private static final int MAX_REPEATS = 5;
    private Console command;

    public RepeatCommand(Console command) {
        super(NAME, "Repeats the last n commands", "repeat <number of commands to repeat>");
        this.command = command;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        List<String> argList = cmdLine.getArgList();
        if (argList.size() != 1) {
            out.println(this.obtainUsage());
            return;
        }
        try {
            int numOfCommandsToRepeat = Integer.parseInt(argList.get(0));
            if (numOfCommandsToRepeat > MAX_REPEATS) {
                out.println("Error cannot perform more than " + MAX_REPEATS + " repeats.");
                return;
            }
            List<String> history = command.history();
            int size = history.size();

            // we need size - 1 because the most recent repeat command is in the
            // history, but we do not count it
            if (size - 1 < numOfCommandsToRepeat) {
                out.println("Error: cannot repeat " + numOfCommandsToRepeat + " commands when only " + (size - 1)
                        + " have been executed");
            } else if (numOfCommandsToRepeat > 0) {
                for (int a = size - numOfCommandsToRepeat - 1; a < size - 1; a++) {
                    executeCoach(out, history, a);
                }
            }
        } catch (NumberFormatException e) {
            out.println(this.obtainUsage());
        } catch (IOException e) {
            out.println(e.getMessage());
        }
    }

    private void executeCoach(PrintStream out, List<String> history, int i) throws IOException {
        String command = history.get(i);
        // print command so user can see what command is being executed
        out.println(command);

        // check that we are not repeating the repeat command
        String[] commandArgs = command.split(" ");
        if (!commandArgs[0].equalsIgnoreCase(NAME)) {
            this.command.executeCommand(command, false);
        } else {
            out.println("Cannot repeat a repeat command");
        }
    }
}