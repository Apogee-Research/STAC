package com.ahancock.enigma;

public class English {

    private int errorsAllowed;

    private static final double[] letterFreq = { 8.1, 1.6, 3.2, 3.6, 12.3, 2.3, 1.6, 5.1, 7.2, 0.1, 0.5, 4.0, 2.2, 7.2, 7.9, 2.3, 0.2, 6.0, 6.6, 9.6, 3.1, 0.9, 2.0, 0.2, 1.9, 0.1 };

    private static final int[] letterDeviation = { 10, 50, 30, 30, 10, 30, 50, 20, 15, 100, 80, 30, 30, 20, 20, 30, 100, 30, 20, 15, 30, 60, 40, 100, 40, 100 };

    // The upper frequency bounds tunable parameters
    private double[] upperBounds = new double[Symbol.MAX];

    // The lower frequency bounds including tunable parameters
    private double[] lowerBounds = new double[Symbol.MAX];

    // Stores the frequency of all encryptable symbols seen according to the
    // Symbol offset
    private int[] charCounts = new int[Symbol.MAX];

    // The total count of all encryptable symbols seen
    private int totalChars;

    public English(int errorsAllowed, double multiplier) {
        this.errorsAllowed = errorsAllowed;
        // multiplier
        for (int i = 0; i < Symbol.MAX; i++) {
            double letterFrequency = letterFreq[i] / 100;
            double deviation = letterFrequency * letterDeviation[i] * multiplier / 100.0;
            lowerBounds[i] = letterFrequency + deviation;
            upperBounds[i] = letterFrequency - deviation;
        }
    }

    // Adding a line counts the frequency of encryptable characters in the
    // line and increment the total number of encryptable characters
    public void add(String line) {
        for (int i = 0; i < line.length(); i++) {
            char currentChar = line.charAt(i);
            // Characters which are not encryptable are not considered
            if (Symbol.ignoreSymbol(currentChar))
                continue;
            charCounts[Symbol.getOffset(currentChar)]++;
            totalChars++;
        }
    }

    // Compute the number of symbol frequencies which exceed the bounds and
    // return true if the number of errors does not exceed errorsAllowed
    public boolean isEnglish() {
        int errorCount = 0;
        for (int i = 0; i < Symbol.MAX; i++) {
            double frequency = (double) charCounts[i] / totalChars;
            if (frequency > lowerBounds[i] && frequency > upperBounds[i]) {
                errorCount++;
            }
        }
        return errorCount <= errorsAllowed;
    }

    // Resets the state of the object before analyzing a new section of text
    public void reset() {
        charCounts = new int[Symbol.MAX];
        totalChars = 0;
    }
}
