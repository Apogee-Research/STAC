package com.techtip.control;

public class ExitCommandBuilder {
    private Ui ui;

    public ExitCommandBuilder assignUi(Ui ui) {
        this.ui = ui;
        return this;
    }

    public ExitCommand formExitCommand() {
        return new ExitCommand(ui);
    }
}