package org.digitaltip.ui;

import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;
import java.util.List;

public class HistoryCommand extends Command {
    private static final String NAME = "history";
    private Display display;
    
    public HistoryCommand(Display display) {
        super(NAME, "prints the command history", NAME);
        this.display = display;
    }
    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        List<String> history = display.history();
        
        // don't include the last command because that is the
        // most recent history command
        for (int c = 0; c < history.size() - 1; c++) {
            executeUtility(out, history, c);
        }
    }

    private void executeUtility(PrintStream out, List<String> history, int q) {
        out.println(history.get(q));
    }

}