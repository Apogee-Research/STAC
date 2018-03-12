package net.robotictip.slf4j.inplace;

public class SimpleLoggerBuilder {
    private String name;

    public SimpleLoggerBuilder assignName(String name) {
        this.name = name;
        return this;
    }

    public SimpleLogger generateSimpleLogger() {
        return new SimpleLogger(name);
    }
}