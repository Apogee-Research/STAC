package com.digitalpoint.terminal;

public class ExitCommandBuilder {
    private Console command;

    public ExitCommandBuilder defineCommand(Console command) {
        this.command = command;
        return this;
    }

    public ExitCommand makeExitCommand() {
        return new ExitCommand(command);
    }
}