package com.digitalpoint.terminal;

public class RepeatCommandBuilder {
    private Console command;

    public RepeatCommandBuilder defineCommand(Console command) {
        this.command = command;
        return this;
    }

    public RepeatCommand makeRepeatCommand() {
        return new RepeatCommand(command);
    }
}