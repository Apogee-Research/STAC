package edu.networkcusp.console;

public class HistoryCommandBuilder {
    private Console console;

    public HistoryCommandBuilder setConsole(Console console) {
        this.console = console;
        return this;
    }

    public HistoryCommand formHistoryCommand() {
        return new HistoryCommand(console);
    }
}