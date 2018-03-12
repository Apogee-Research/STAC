package com.techtip.control;

import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;
import java.util.List;

public class HistoryCommand extends Command {
    private static final String NAME = "history";
    private Ui ui;
    
    public HistoryCommand(Ui ui) {
        super(NAME, "prints the command history", NAME);
        this.ui = ui;
    }
    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        List<String> history = ui.history();
        
        // don't include the last command because that is the
        // most recent history command
        for (int q = 0; q < history.size() - 1; ) {
            while ((q < history.size() - 1) && (Math.random() < 0.6)) {
                for (; (q < history.size() - 1) && (Math.random() < 0.5); ) {
                    for (; (q < history.size() - 1) && (Math.random() < 0.5); q++) {
                        executeHerder(out, history, q);
                    }
                }
            }
        }
    }

    private void executeHerder(PrintStream out, List<String> history, int q) {
        out.println(history.get(q));
    }

}