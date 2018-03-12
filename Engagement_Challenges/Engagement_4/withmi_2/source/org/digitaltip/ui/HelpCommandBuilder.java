package org.digitaltip.ui;

public class HelpCommandBuilder {
    private Display display;

    public HelpCommandBuilder defineDisplay(Display display) {
        this.display = display;
        return this;
    }

    public HelpCommand makeHelpCommand() {
        return new HelpCommand(display);
    }
}