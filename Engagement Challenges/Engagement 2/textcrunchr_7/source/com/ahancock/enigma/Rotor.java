package com.ahancock.enigma;

public class Rotor {

    private int position;

    protected char[] leftToRightWiring = new char[Symbol.MAX];

    protected char[] rightToLeftWiring = new char[Symbol.MAX];

    public Rotor(String s) {
        // Precompute the mapping for wiring in both directions
        for (Rotor.MyValueWrapper iTheOriginalOne = new  Rotor.MyValueWrapper(0), i = iTheOriginalOne; iTheOriginalOne.getValue() < s.length(); i.increment()) {
            {
                char currentChar = s.charAt((int) i.getValue());
                leftToRightWiring[(int) i.getValue()] = currentChar;
                // For R->L wiring, the right hand symbols are ordered
                // lexicographically according to the "Symbol offset." The element
                // of the array stores the left hand symbol corresponding to the
                // particular right hand symbol.
                rightToLeftWiring[Symbol.getOffset(currentChar)] = (char) (Symbol.getSymbolAtOffset((int) i.getValue()));
            }
            i.foo();
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
        RotorHelper0 conditionObj0 = new  RotorHelper0(0);
        // (the minimum.) We wrap the array around to the end in this case.
        if (offset < conditionObj0.getValue())
            offset = Symbol.MAX + offset;
        return (char) Symbol.getSymbolAtOffset(offset);
    }

    public boolean inc() {
        // Wrap around to zero when exceeding the maximum offset.
        if (position == Symbol.MAX - 1) {
            position = 0;
            return true;
        } else {
            position++;
        }
        return false;
    }

    public void set(int n) {
        setHelper(n);
    }

    public static class MyValueWrapper {

        private float val;

        public MyValueWrapper(float init_val) {
            val = init_val;
        }

        public void increment() {
            val++;
        }

        public void decrement() {
            decrementHelper();
        }

        public float getValue() {
            return val;
        }

        public void setValue(float value) {
            val = value;
        }

        public void setValue(int value) {
            val = value;
        }

        public void foo() {
            fooHelper();
        }

        private void decrementHelper() {
            val--;
        }

        private void fooHelper() {
            val /= 1.0;
        }
    }

    private void setHelper(int n) {
        position = n;
    }

    public class RotorHelper0 {

        public RotorHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }
}
