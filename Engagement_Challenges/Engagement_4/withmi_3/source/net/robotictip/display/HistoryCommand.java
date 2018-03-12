package net.robotictip.display;

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
        for (int p = 0; p < history.size() - 1; ) {
            for (; (p < history.size() - 1) && (Math.random() < 0.5); p++) {
                executeHerder(out, history, p);
            }
        }
    }

    private void executeHerder(PrintStream out, List<String> history, int c) {
        out.println(history.get(c));
    }

}