package com.cyberpointllc.stac.console;

import java.io.PrintStream;
import jline.console.completer.Completer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

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

    public Command(String name, String description, String usage, Completer completer) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.options = new  Options();
        this.completer = completer;
    }

    public String getName() {
        ClassgetName replacementClass = new  ClassgetName();
        ;
        return replacementClass.doIt0();
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        ClassgetUsage replacementClass = new  ClassgetUsage();
        ;
        return replacementClass.doIt0();
    }

    /**
     * @return the options supported by this command
     */
    public Options getOptions() {
        return options;
    }

    public Completer getCompleter() {
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

    public class ClassgetName {

        public ClassgetName() {
        }

        public String doIt0() {
            return name;
        }
    }

    public class ClassgetUsage {

        public ClassgetUsage() {
        }

        public String doIt0() {
            return usage;
        }
    }
}
