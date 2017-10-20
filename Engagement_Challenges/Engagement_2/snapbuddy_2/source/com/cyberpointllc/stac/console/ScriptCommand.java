package com.cyberpointllc.stac.console;

import java.io.File;
import java.io.PrintStream;
import jline.console.completer.AggregateCompleter;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.FileNameCompleter;
import jline.console.completer.StringsCompleter;
import org.apache.commons.cli.CommandLine;

public class ScriptCommand extends Command {

    public static final String NAME = "script";

    private Console console;

    public ScriptCommand(Console console) {
        super(NAME, "runs a script", "script <script name>", new  AggregateCompleter(new  ArgumentCompleter(new  StringsCompleter(NAME), new  FileNameCompleter())));
        this.console = console;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        try {
            console.runScript(new  File(cmdLine.getArgList().get(0)));
        } catch (Exception e) {
            out.println(e.getMessage());
        }
    }
}
