package org.digitalapex.head;

public class RepeatCommandBuilder {
    private Control control;

    public RepeatCommandBuilder fixControl(Control control) {
        this.control = control;
        return this;
    }

    public RepeatCommand generateRepeatCommand() {
        return new RepeatCommand(control);
    }
}