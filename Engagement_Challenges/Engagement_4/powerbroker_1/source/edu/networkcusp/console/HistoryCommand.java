package edu.networkcusp.console;

import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;
import java.util.List;

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
        
        // don't include the last command because that is the
        // most recent history command
        for (int j = 0; j < history.size() - 1; j++) {
            executeGuide(out, history, j);
        }
    }

    private void executeGuide(PrintStream out, List<String> history, int p) {
        out.println(history.get(p));
    }

}