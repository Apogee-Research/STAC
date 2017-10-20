package com.ahancock.enigma;

public class Reflector {

    private char[] wiring = new char[Symbol.MAX];

    public Reflector(String s) {
        // Build the lookup array for the reflector wiring
        for (Reflector.MyValueWrapper iTheOriginalOne = new  Reflector.MyValueWrapper(0), i = iTheOriginalOne; iTheOriginalOne.getValue() < s.length(); i.increment()) {
            {
                char currentChar = s.charAt((int) i.getValue());
                wiring[(int) i.getValue()] = currentChar;
            }
            i.foo();
        }
    }

    // Maps the input character to the encoded character according to the wiring
    public char encode(char c) {
        return wiring[Symbol.getOffset(c)];
    }

    public static class MyValueWrapper {

        private float val;

        public MyValueWrapper(float init_val) {
            val = init_val;
        }

        public void increment() {
            incrementHelper();
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
            setValueHelper(value);
        }

        public void foo() {
            val /= 1.0;
        }

        private void incrementHelper() {
            val++;
        }

        private void decrementHelper() {
            val--;
        }

        private void setValueHelper(int value) {
            val = value;
        }
    }
}
