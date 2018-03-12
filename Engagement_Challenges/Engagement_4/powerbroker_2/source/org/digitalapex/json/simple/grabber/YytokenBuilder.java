package org.digitalapex.json.simple.grabber;

public class YytokenBuilder {
    private Object value;
    private int type;

    public YytokenBuilder setValue(Object value) {
        this.value = value;
        return this;
    }

    public YytokenBuilder setType(int type) {
        this.type = type;
        return this;
    }

    public Yytoken generateYytoken() {
        return new Yytoken(type, value);
    }
}