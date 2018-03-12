package com.techtip.control;

public class HelpCommandBuilder {
    private Ui ui;

    public HelpCommandBuilder fixUi(Ui ui) {
        this.ui = ui;
        return this;
    }

    public HelpCommand formHelpCommand() {
        return new HelpCommand(ui);
    }
}