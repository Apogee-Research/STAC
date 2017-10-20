package com.cyberpointllc.stac.textcrunchr;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CharacterCountProcessor extends Processor {

    private static final String NAME = "characterCount";

    public TCResult process(InputStream inps) throws IOException {
        InputStreamReader isr = new  InputStreamReader(inps);
        // count number of characters
        char buffer[] = new char[10000];
        int bytes_read = isr.read(buffer, 0, buffer.length);
        String value;
        if (bytes_read >= buffer.length) {
            value = ">10,000 characters";
        } else {
            value = bytes_read + " characters";
        }
        return new  TCResult("Character Count", value);
    }

    public String getName() {
        return NAME;
    }
}
