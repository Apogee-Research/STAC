package org.techpoint.logging.helpers;

public class FormattingTupleBuilder {
    private Object[] argArray = null;
    private String message;
    private Throwable throwable = null;

    public FormattingTupleBuilder assignArgArray(Object[] argArray) {
        this.argArray = argArray;
        return this;
    }

    public FormattingTupleBuilder setMessage(String message) {
        this.message = message;
        return this;
    }

    public FormattingTupleBuilder fixThrowable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    public FormattingTuple composeFormattingTuple() {
        return new FormattingTuple(message, argArray, throwable);
    }
}