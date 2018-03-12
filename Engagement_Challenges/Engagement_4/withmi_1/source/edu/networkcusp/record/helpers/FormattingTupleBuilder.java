package edu.networkcusp.record.helpers;

public class FormattingTupleBuilder {
    private Object[] argArray = null;
    private String message;
    private Throwable throwable = null;

    public FormattingTupleBuilder fixArgArray(Object[] argArray) {
        this.argArray = argArray;
        return this;
    }

    public FormattingTupleBuilder assignMessage(String message) {
        this.message = message;
        return this;
    }

    public FormattingTupleBuilder fixThrowable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    public FormattingTuple createFormattingTuple() {
        return new FormattingTuple(message, argArray, throwable);
    }
}