package com.virtualpoint.part.simple.retriever;

public class YytokenBuilder {
    private Object value;
    private int type;

    public YytokenBuilder defineValue(Object value) {
        this.value = value;
        return this;
    }

    public YytokenBuilder defineType(int type) {
        this.type = type;
        return this;
    }

    public Yytoken composeYytoken() {
        return new Yytoken(type, value);
    }
}