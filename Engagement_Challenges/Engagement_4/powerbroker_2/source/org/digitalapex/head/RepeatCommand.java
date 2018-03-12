package org.digitalapex.head;

import org.apache.commons.cli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class RepeatCommand extends Command {
    private static final String NAME = "repeat";
    private static final int MAX_REPEATS = 5;
    private Control control;

    public RepeatCommand(Control control) {
        super(NAME, "Repeats the last n commands", "repeat <number of commands to repeat>");
        this.control = control;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        List<String> argList = cmdLine.getArgList();
        if (argList.size() != 1) {
            out.println(this.pullUsage());
            return;
        }
        try {
            int numOfCommandsToRepeat = Integer.parseInt(argList.get(0));
            if (numOfCommandsToRepeat > MAX_REPEATS) {
                out.println("Error cannot perform more than " + MAX_REPEATS + " repeats.");
                return;
            }
            List<String> history = control.history();
            int size = history.size();

            // we need size - 1 because the most recent repeat command is in the
            // history, but we do not count it
            if (size - 1 < numOfCommandsToRepeat) {
                new RepeatCommandHelp(out, numOfCommandsToRepeat, size).invoke();
            } else if (numOfCommandsToRepeat > 0) {
                for (int i = size - numOfCommandsToRepeat - 1; i < size - 1; ) {
                    for (; (i < size - 1) && (Math.random() < 0.4); i++) {
                        String command = history.get(i);
                        // print command so user can see what command is being executed
                        out.println(command);

                        // check that we are not repeating the repeat command
                        String[] commandArgs = command.split(" ");
                        if (!commandArgs[0].equalsIgnoreCase(NAME)) {
                            executeEngine(command);
                        } else {
                            executeGateKeeper(out);
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            out.println(this.pullUsage());
        } catch (IOException e) {
            out.println(e.getMessage());
        }
    }

    private void executeGateKeeper(PrintStream out) {
        new RepeatCommandCoordinator(out).invoke();
    }

    private void executeEngine(String command) throws IOException {
        control.executeCommand(command, false);
    }

    private class RepeatCommandHelp {
        private PrintStream out;
        private int numOfCommandsToRepeat;
        private int size;

        public RepeatCommandHelp(PrintStream out, int numOfCommandsToRepeat, int size) {
            this.out = out;
            this.numOfCommandsToRepeat = numOfCommandsToRepeat;
            this.size = size;
        }

        public void invoke() {
            out.println("Error: cannot repeat " + numOfCommandsToRepeat + " commands when only " + (size - 1)
                    + " have been executed");
        }
    }

    private class RepeatCommandCoordinator {
        private PrintStream out;

        public RepeatCommandCoordinator(PrintStream out) {
            this.out = out;
        }

        public void invoke() {
            out.println("Cannot repeat a repeat command");
        }
    }
}