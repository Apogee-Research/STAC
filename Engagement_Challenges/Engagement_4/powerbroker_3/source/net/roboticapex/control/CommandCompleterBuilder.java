package net.roboticapex.control;

public class CommandCompleterBuilder {
    private Display display;

    public CommandCompleterBuilder fixDisplay(Display display) {
        this.display = display;
        return this;
    }

    public CommandCompleter makeCommandCompleter() {
        return new CommandCompleter(display);
    }
}