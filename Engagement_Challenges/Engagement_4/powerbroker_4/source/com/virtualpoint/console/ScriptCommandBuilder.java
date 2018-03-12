package com.virtualpoint.console;

public class ScriptCommandBuilder {
    private Display display;

    public ScriptCommandBuilder fixDisplay(Display display) {
        this.display = display;
        return this;
    }

    public ScriptCommand composeScriptCommand() {
        return new ScriptCommand(display);
    }
}