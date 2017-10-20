package com.ahancock.enigma;

public class Test {

    public static void main(String[] args) {
        Classmain replacementClass = new  Classmain(args);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        replacementClass.doIt2();
        replacementClass.doIt3();
        replacementClass.doIt4();
    }

    public static class Classmain {

        public Classmain(String[] args) {
            this.args = args;
        }

        private String[] args;

        private EnigmaMachine machine;

        public void doIt0() {
            machine = EnigmaFactory.buildEnigmaMachine();
        }

        private String test;

        public void doIt1() {
            test = "AAAAAAAAAAAAAAAAAAAAAAAAAAA";
            System.out.println(test);
        }

        public void doIt2() {
            // Set the rotors, encrypt the string and print the results
            machine.setRotors(5, 9, 14);
        }

        private String encodedString;

        public void doIt3() {
            encodedString = machine.encodeLine(test);
            System.out.println(encodedString);
            // Reset the rotors, decrypt the string and print the results
            machine.setRotors(5, 9, 14);
        }

        public void doIt4() {
            System.out.println(machine.encodeLine(encodedString));
        }
    }
}
