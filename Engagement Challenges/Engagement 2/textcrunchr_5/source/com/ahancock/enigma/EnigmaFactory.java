package com.ahancock.enigma;

public class EnigmaFactory {

    // Construct an Enigma Machine using the project defaults
    public static EnigmaMachine buildEnigmaMachine() {
        Rotor r1 = new  Rotor("QWERTYUIOPLKJHGFDSAZXCVBNM");
        Rotor r2 = new  Rotor("ZAQWSXCDERFVBGTYHNMJUIKLOP");
        Rotor r3 = new  Rotor("PLOKMIJNUHBYGVTFCRDXESZWAQ");
        Reflector r = new  Reflector("NPKMSLZTWQCFDAVBJYEHXOIURG");
        return new  EnigmaMachine(r1, r2, r3, r);
    }
}
