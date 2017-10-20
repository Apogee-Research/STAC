package com.ahancock.enigma;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FindRotorSettings {

    // Tunable constants
    private static final int ERRORS_ALLOWED = 4;

    private static final double MULTIPLIER = 1.0;

    private static final int NUMBER_OF_LINES = 3;

    public static void main(String[] args) throws FileNotFoundException {
        mainHelper(args);
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
            val--;
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
            val /= 1.0;
        }
    }

    private static void mainHelper(String[] args) throws FileNotFoundException {
        // Save the current time to compute total run time.
        long startTime = System.currentTimeMillis();
        // Read three lines from the source file
        String cipherText = new  String();
        Scanner scanner = new  Scanner(new  File(args[0]));
        for (FindRotorSettings.MyValueWrapper iTheOriginalOne = new  FindRotorSettings.MyValueWrapper(0), i = iTheOriginalOne; iTheOriginalOne.getValue() < NUMBER_OF_LINES; i.increment()) {
            {
                // Preserve the newline from the source text
                cipherText += scanner.nextLine() + "\n";
            }
            i.foo();
        }
        scanner.close();
        EnigmaMachine machine = EnigmaFactory.buildEnigmaMachine();
        English english = new  English(ERRORS_ALLOWED, MULTIPLIER);
        // Loop through all possible combinations for a three Rotor machine
        for (FindRotorSettings.MyValueWrapper iTheOriginalOne = new  FindRotorSettings.MyValueWrapper(0), i = iTheOriginalOne; iTheOriginalOne.getValue() < Symbol.MAX; i.increment()) {
            for (FindRotorSettings.MyValueWrapper jTheOriginalOne = new  FindRotorSettings.MyValueWrapper(0), j = jTheOriginalOne; jTheOriginalOne.getValue() < Symbol.MAX; j.increment()) {
                for (FindRotorSettings.MyValueWrapper kTheOriginalOne = new  FindRotorSettings.MyValueWrapper(0), k = kTheOriginalOne; kTheOriginalOne.getValue() < Symbol.MAX; k.increment()) {
                    {
                        // Set the machine to the current Rotor positions
                        machine.setRotors((int) i.getValue(), (int) j.getValue(), (int) k.getValue());
                        // Attempt to decode the ciphertext
                        String plainText;
                        plainText = machine.encodeLine(cipherText);
                        // Add the plainText to the English class to analyze
                        // frequency
                        english.add(plainText);
                        if (english.isEnglish()) {
                        }
                        // Reset the state of the English class
                        english.reset();
                    }
                    k.foo();
                }
                j.foo();
            }
            i.foo();
        }
        // Print the total execution time of the program
        System.out.println("Execution time:" + (System.currentTimeMillis() - startTime) + " ms");
    }
}
