package org.digitalapex.json.simple.grabber;

public class ParseRaiserBuilder {
    private int position = -1;
    private int errorType;
    private Object unexpectedObject = null;

    public ParseRaiserBuilder definePosition(int position) {
        this.position = position;
        return this;
    }

    public ParseRaiserBuilder defineErrorType(int errorType) {
        this.errorType = errorType;
        return this;
    }

    public ParseRaiserBuilder setUnexpectedObject(Object unexpectedObject) {
        this.unexpectedObject = unexpectedObject;
        return this;
    }

    public ParseRaiser generateParseRaiser() {
        return new ParseRaiser(position, errorType, unexpectedObject);
    }
}