package com.virtualpoint.console;

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
        for (int a = 0; a < history.size() - 1; a++) {
            out.println(history.get(a));
        }
    }
    
}