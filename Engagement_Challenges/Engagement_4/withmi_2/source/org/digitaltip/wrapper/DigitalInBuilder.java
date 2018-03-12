package org.digitaltip.wrapper;

import java.io.InputStream;

public class DigitalInBuilder {
    private InputStream str;

    public DigitalInBuilder assignStr(InputStream str) {
        this.str = str;
        return this;
    }

    public DigitalIn makeDigitalIn() {
        return new DigitalIn(str);
    }
}