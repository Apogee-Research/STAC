package com.cyberpointllc.stac.console;

import java.io.PrintStream;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import java.util.Random;

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
        for (int i = 0; i < history.size() - 1; ) {
            Random randomNumberGeneratorInstance = new  Random();
            for (; i < history.size() - 1 && randomNumberGeneratorInstance.nextDouble() < 0.9; i++) {
                out.println(history.get(i));
            }
        }
    }
}
