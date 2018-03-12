package net.cybertip.note.actual;

public class SimpleLoggerBuilder {
    private String name;

    public SimpleLoggerBuilder assignName(String name) {
        this.name = name;
        return this;
    }

    public SimpleLogger makeSimpleLogger() {
        return new SimpleLogger(name);
    }
}