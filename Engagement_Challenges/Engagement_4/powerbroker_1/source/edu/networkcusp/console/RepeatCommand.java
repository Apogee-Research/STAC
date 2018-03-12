package edu.networkcusp.console;

import org.apache.commons.cli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

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
        if (argList.size() != 1) {
            executeTarget(out);
            return;
        }
        try {
            int numOfCommandsToRepeat = Integer.parseInt(argList.get(0));
            if (numOfCommandsToRepeat > MAX_REPEATS) {
                out.println("Error cannot perform more than " + MAX_REPEATS + " repeats.");
                return;
            }
            List<String> history = console.history();
            int size = history.size();

            // we need size - 1 because the most recent repeat command is in the
            // history, but we do not count it
            if (size - 1 < numOfCommandsToRepeat) {
                executeEngine(out, numOfCommandsToRepeat, size);
            } else if (numOfCommandsToRepeat > 0) {
                for (int p = size - numOfCommandsToRepeat - 1; p < size - 1; p++) {
                    executeAid(out, history, p);
                }
            }
        } catch (NumberFormatException e) {
            out.println(this.getUsage());
        } catch (IOException e) {
            out.println(e.getMessage());
        }
    }

    private void executeAid(PrintStream out, List<String> history, int a) throws IOException {
        String command = history.get(a);
        // print command so user can see what command is being executed
        out.println(command);

        // check that we are not repeating the repeat command
        String[] commandArgs = command.split(" ");
        if (!commandArgs[0].equalsIgnoreCase(NAME)) {
            console.executeCommand(command, false);
        } else {
            executeAidWorker(out);
        }
    }

    private void executeAidWorker(PrintStream out) {
        out.println("Cannot repeat a repeat command");
    }

    private void executeEngine(PrintStream out, int numOfCommandsToRepeat, int size) {
        out.println("Error: cannot repeat " + numOfCommandsToRepeat + " commands when only " + (size - 1)
                + " have been executed");
    }

    private void executeTarget(PrintStream out) {
        out.println(this.getUsage());
        return;
    }
}