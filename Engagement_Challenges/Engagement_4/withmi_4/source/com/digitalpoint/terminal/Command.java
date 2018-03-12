package com.digitalpoint.terminal;

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

    public String fetchName() {
        return name;
    }

    public String pullDescription() {
        return description;
    }

    public String obtainUsage() {
        return usage;
    }

    /**
     * @return the options supported by this command
     */
    public Options pullOptions() {
        return options;
    }

    public Completer takeCompleter() {
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
