package com.digitalpoint.terminal;

import jline.console.completer.AggregateCompleter;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.FileNameCompleter;
import jline.console.completer.StringsCompleter;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.io.PrintStream;

public class ScriptCommand extends Command {
    public static final String NAME = "script";
    private Console command;

    public ScriptCommand(Console command) {
        super(NAME, "runs a script", "script <script name>",
                new AggregateCompleter(new ArgumentCompleter(new StringsCompleter(NAME), new FileNameCompleter())));
        this.command = command;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        try {
            command.runScript(new File(cmdLine.getArgList().get(0)));
        } catch (Exception e) {
            out.println(e.getMessage());
        }
    }
}