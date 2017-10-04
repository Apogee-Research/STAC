package com.cyberpointllc.stac.console;

import java.io.PrintStream;
import java.util.List;
import org.apache.commons.cli.CommandLine;

public class HistoryCommand extends Command {

    private static final String NAME = "history";

    private Console console;

    public HistoryCommand(Console console) {
        super(NAME, "prints the command history", NAME);
        this.console = console;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        List<String> history = console.history();
        // most recent history command
        for (int i = 0; i < history.size() - 1; i++) {
            executeHelper(history, out, i);
        }
    }

    private void executeHelper(List<String> history, PrintStream out, int i) {
        out.println(history.get(i));
    }
}
