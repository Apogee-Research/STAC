package com.cyberpointllc.stac.console;

import java.io.PrintStream;
import org.apache.commons.cli.CommandLine;

public class ExitCommand extends Command {

    private Console console;

    public ExitCommand(Console console) {
        super("exit", "Exits the program");
        this.console = console;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        executeHelper(out);
    }

    private void executeHelper(PrintStream out) {
        out.println("Goodbye");
        console.setShouldExit(true);
    }
}
