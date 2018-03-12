package edu.networkcusp.console;

public class ScriptCommandBuilder {
    private Console console;

    public ScriptCommandBuilder fixConsole(Console console) {
        this.console = console;
        return this;
    }

    public ScriptCommand formScriptCommand() {
        return new ScriptCommand(console);
    }
}