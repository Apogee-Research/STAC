package com.ahancock.enigma;

public class Reflector {

    private char[] wiring = new char[Symbol.MAX];

    public Reflector(String s) {
        // Build the lookup array for the reflector wiring
        for (int i = 0; i < s.length(); i++) {
            char currentChar = s.charAt(i);
            wiring[i] = currentChar;
        }
    }

    // Maps the input character to the encoded character according to the wiring
    public char encode(char c) {
        return wiring[Symbol.getOffset(c)];
    }
}
