package com.ahancock.enigma;

public class Rotor {

    private int position;

    protected char[] leftToRightWiring = new char[Symbol.MAX];

    protected char[] rightToLeftWiring = new char[Symbol.MAX];

    public Rotor(String s) {
        // Precompute the mapping for wiring in both directions
        for (int i = 0; i < s.length(); i++) {
            char currentChar = s.charAt(i);
            leftToRightWiring[i] = currentChar;
            // For R->L wiring, the right hand symbols are ordered
            // lexicographically according to the "Symbol offset." The element
            // of the array stores the left hand symbol corresponding to the
            // particular right hand symbol.
            rightToLeftWiring[Symbol.getOffset(currentChar)] = (char) (Symbol.getSymbolAtOffset(i));
        }
    }

    public char encodeLR(char c) {
        // The position must be added to the offset to get the correct mapping
        int index = Symbol.getOffset(c) + position;
        // symbols it wraps around to the beginning
        if (index > Symbol.MAX - 1)
            index -= Symbol.MAX;
        return leftToRightWiring[index];
    }

    public char encodeRL(char c) {
        // For R->L, we must get the offset of the corresponding left hand side
        // before subtracting position. This is because only the left hand side
        // of the Rotor is in lexicographical order.
        // Note: Position is subtracted, rather than added as in L->R
        int offset = Symbol.getOffset((char) (rightToLeftWiring[Symbol.getOffset(c)] - position));
        // (the minimum.) We wrap the array around to the end in this case.
        if (offset < 0)
            offset = Symbol.MAX + offset;
        return (char) Symbol.getSymbolAtOffset(offset);
    }

    public boolean inc() {
        // Wrap around to zero when exceeding the maximum offset.
        if (position == Symbol.MAX - 1) {
            position = 0;
            return true;
        } else {
            incHelper();
        }
        return false;
    }

    public void set(int n) {
        setHelper(n);
    }

    private void incHelper() {
        position++;
    }

    private void setHelper(int n) {
        position = n;
    }
}
