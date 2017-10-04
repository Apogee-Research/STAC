package com.ahancock.enigma;

public class Test {

    public static void main(String[] args) {
        // Construct the machine
        EnigmaMachine machine = EnigmaFactory.buildEnigmaMachine();
        // Print the test string
        String test = "AAAAAAAAAAAAAAAAAAAAAAAAAAA";
        System.out.println(test);
        // Set the rotors, encrypt the string and print the results
        machine.setRotors(5, 9, 14);
        String encodedString = machine.encodeLine(test);
        System.out.println(encodedString);
        // Reset the rotors, decrypt the string and print the results
        machine.setRotors(5, 9, 14);
        System.out.println(machine.encodeLine(encodedString));
    }
}
