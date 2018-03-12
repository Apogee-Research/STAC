package net.roboticapex.parser.simple.grabber;

public class ParseDeviationBuilder {
    private int position = -1;
    private int errorType;
    private Object unexpectedObject = null;

    public ParseDeviationBuilder fixPosition(int position) {
        this.position = position;
        return this;
    }

    public ParseDeviationBuilder setErrorType(int errorType) {
        this.errorType = errorType;
        return this;
    }

    public ParseDeviationBuilder setUnexpectedObject(Object unexpectedObject) {
        this.unexpectedObject = unexpectedObject;
        return this;
    }

    public ParseDeviation makeParseDeviation() {
        return new ParseDeviation(position, errorType, unexpectedObject);
    }
}