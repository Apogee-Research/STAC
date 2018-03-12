package net.robotictip.slf4j.helpers;

public class FormattingTupleBuilder {
    private Object[] argArray = null;
    private String message;
    private Throwable throwable = null;

    public FormattingTupleBuilder defineArgArray(Object[] argArray) {
        this.argArray = argArray;
        return this;
    }

    public FormattingTupleBuilder defineMessage(String message) {
        this.message = message;
        return this;
    }

    public FormattingTupleBuilder fixThrowable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    public FormattingTuple generateFormattingTuple() {
        return new FormattingTuple(message, argArray, throwable);
    }
}