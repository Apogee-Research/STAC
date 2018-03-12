package net.robotictip.display;

public class HistoryCommandBuilder {
    private Display display;

    public HistoryCommandBuilder setDisplay(Display display) {
        this.display = display;
        return this;
    }

    public HistoryCommand generateHistoryCommand() {
        return new HistoryCommand(display);
    }
}