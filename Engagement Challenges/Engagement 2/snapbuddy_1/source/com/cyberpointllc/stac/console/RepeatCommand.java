package com.cyberpointllc.stac.console;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import org.apache.commons.cli.CommandLine;

public class RepeatCommand extends Command {

    private static final String NAME = "repeat";

    private static final int MAX_REPEATS = 5;

    private Console console;

    public RepeatCommand(Console console) {
        super(NAME, "Repeats the last n commands", "repeat <number of commands to repeat>");
        this.console = console;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        List<String> argList = cmdLine.getArgList();
        int conditionObj0 = 1;
        if (argList.size() != conditionObj0) {
            out.println(this.getUsage());
            return;
        }
        int conditionObj1 = 0;
        try {
            int numOfCommandsToRepeat = Integer.parseInt(argList.get(0));
            if (numOfCommandsToRepeat > MAX_REPEATS) {
                out.println("Error cannot perform more than " + MAX_REPEATS + " repeats.");
                return;
            }
            List<String> history = console.history();
            int size = history.size();
            // history, but we do not count it
            if (size - 1 < numOfCommandsToRepeat) {
                out.println("Error: cannot repeat " + numOfCommandsToRepeat + " commands when only " + (size - 1) + " have been executed");
            } else if (numOfCommandsToRepeat > conditionObj1) {
                for (int i = size - numOfCommandsToRepeat - 1; i < size - 1; i++) {
                    String command = history.get(i);
                    // print command so user can see what command is being executed
                    out.println(command);
                    // check that we are not repeating the repeat command
                    String[] commandArgs = command.split(" ");
                    if (!commandArgs[0].equalsIgnoreCase(NAME)) {
                        console.executeCommand(command, false);
                    } else {
                        executeHelper(out);
                    }
                }
            }
        } catch (NumberFormatException e) {
            out.println(this.getUsage());
        } catch (IOException e) {
            out.println(e.getMessage());
        }
    }

    private void executeHelper(PrintStream out) throws IOException, NumberFormatException {
        out.println("Cannot repeat a repeat command");
    }
}
