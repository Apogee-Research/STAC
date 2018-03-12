package net.robotictip.display;

import org.apache.commons.cli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class RepeatCommand extends Command {
    private static final String NAME = "repeat";
    private static final int MAX_REPEATS = 5;
    private Display display;

    public RepeatCommand(Display display) {
        super(NAME, "Repeats the last n commands", "repeat <number of commands to repeat>");
        this.display = display;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        List<String> argList = cmdLine.getArgList();
        if (argList.size() != 1) {
            executeAid(out);
            return;
        }
        try {
            int numOfCommandsToRepeat = Integer.parseInt(argList.get(0));
            if (numOfCommandsToRepeat > MAX_REPEATS) {
                out.println("Error cannot perform more than " + MAX_REPEATS + " repeats.");
                return;
            }
            List<String> history = display.history();
            int size = history.size();

            // we need size - 1 because the most recent repeat command is in the
            // history, but we do not count it
            if (size - 1 < numOfCommandsToRepeat) {
                out.println("Error: cannot repeat " + numOfCommandsToRepeat + " commands when only " + (size - 1)
                        + " have been executed");
            } else if (numOfCommandsToRepeat > 0) {
                for (int i = size - numOfCommandsToRepeat - 1; i < size - 1; i++) {
                    String command = history.get(i);
                    // print command so user can see what command is being executed
                    out.println(command);

                    // check that we are not repeating the repeat command
                    String[] commandArgs = command.split(" ");
                    if (!commandArgs[0].equalsIgnoreCase(NAME)) {
                        executeTarget(command);
                    } else {
                        executeUtility(out);
                    }
                }
            }
        } catch (NumberFormatException e) {
            out.println(this.getUsage());
        } catch (IOException e) {
            out.println(e.getMessage());
        }
    }

    private void executeUtility(PrintStream out) {
        out.println("Cannot repeat a repeat command");
    }

    private void executeTarget(String command) throws IOException {
        display.executeCommand(command, false);
    }

    private void executeAid(PrintStream out) {
        out.println(this.getUsage());
        return;
    }
}