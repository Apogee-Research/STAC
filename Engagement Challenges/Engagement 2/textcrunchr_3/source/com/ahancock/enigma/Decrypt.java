package com.ahancock.enigma;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Decrypt {

    public static void main(String[] args) throws FileNotFoundException {
        mainHelper(args);
    }

    private static void mainHelper(String[] args) throws FileNotFoundException {
        EnigmaMachine machine = EnigmaFactory.buildEnigmaMachine();
        machine.setRotors(19, 23, 8);
        Scanner scanner = new  Scanner(new  File(args[0]));
        while (scanner.hasNext()) {
            System.out.println(machine.encodeLine(scanner.nextLine()));
        }
        scanner.close();
    }
}
