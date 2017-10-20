package com.ahancock.enigma;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Decrypt {

    public static void main(String[] args) throws FileNotFoundException {
        EnigmaMachine machine = EnigmaFactory.buildEnigmaMachine();
        machine.setRotors(19, 23, 8);
        Scanner scanner = new  Scanner(new  File(args[0]));
        while (scanner.hasNext()) {
            mainHelper(machine, scanner);
        }
        scanner.close();
    }

    private static void mainHelper(EnigmaMachine machine, Scanner scanner) throws FileNotFoundException {
        System.out.println(machine.encodeLine(scanner.nextLine()));
    }
}
