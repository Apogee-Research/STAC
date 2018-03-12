package org.digitaltip.ui;

public class ScriptCommandBuilder {
    private Display display;

    public ScriptCommandBuilder defineDisplay(Display display) {
        this.display = display;
        return this;
    }

    public ScriptCommand makeScriptCommand() {
        return new ScriptCommand(display);
    }
}