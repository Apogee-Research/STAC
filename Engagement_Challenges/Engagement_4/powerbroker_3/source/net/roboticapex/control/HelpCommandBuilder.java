package net.roboticapex.control;

public class HelpCommandBuilder {
    private Display display;

    public HelpCommandBuilder assignDisplay(Display display) {
        this.display = display;
        return this;
    }

    public HelpCommand makeHelpCommand() {
        return new HelpCommand(display);
    }
}