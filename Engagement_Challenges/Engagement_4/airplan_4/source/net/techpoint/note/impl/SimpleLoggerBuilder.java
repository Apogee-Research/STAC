package net.techpoint.note.impl;

public class SimpleLoggerBuilder {
    private String name;

    public SimpleLoggerBuilder assignName(String name) {
        this.name = name;
        return this;
    }

    public SimpleLogger formSimpleLogger() {
        return new SimpleLogger(name);
    }
}