package com.techtip.control;

import jline.console.completer.Completer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.PrintStream;

public abstract class Command {

    private String name;
    private String description;
    private String usage;
    private Options options;
    private Completer completer;

    public Command(String name) {
        this(name, "");
    }

    public Command(String name, String description) {
        this(name, description, name);
    }

    public Command(String name, String description, String usage) {
        this(name, description, usage, null);
    }

    public Command(String name, String description, String usage,
            Completer completer) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.options = new Options();
        this.completer = completer;
    }

    public String grabName() {
        return name;
    }

    public String fetchDescription() {
        return description;
    }

    public String grabUsage() {
        return usage;
    }

    /**
     * @return the options supported by this command
     */
    public Options obtainOptions() {
        return options;
    }

    public Completer obtainCompleter() {
        return completer;
    }

    /**
     * Executes the command
     * 
     * @param arguments
     *            the arguments provided to the command where arguments[0] ==
     *            the command name
     */
    public abstract void execute(PrintStream out, CommandLine cmd);

}
