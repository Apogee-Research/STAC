package com.digitalpoint.terminal;

import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;
import java.util.List;

public class HistoryCommand extends Command {
    private static final String NAME = "history";
    private Console command;
    
    public HistoryCommand(Console command) {
        super(NAME, "prints the command history", NAME);
        this.command = command;
    }
    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        List<String> history = command.history();
        
        // don't include the last command because that is the
        // most recent history command
        for (int q = 0; q < history.size() - 1; q++) {
            out.println(history.get(q));
        }
    }
    
}