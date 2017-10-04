package com.cyberpointllc.stac.textcrunchr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import com.ahancock.enigma.EnigmaFactory;
import com.ahancock.enigma.EnigmaMachine;

class EnigmaProcessor extends Processor {

    private static final String NAME = "enigma";

    public TCResult process(InputStream inps) throws IOException {
        Classprocess replacementClass = new  Classprocess(inps);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        replacementClass.doIt2();
        replacementClass.doIt3();
        replacementClass.doIt4();
        return replacementClass.doIt5();
    }

    public String getName() {
        return NAME;
    }

    public class Classprocess {

        public Classprocess(InputStream inps) throws IOException {
            this.inps = inps;
        }

        private InputStream inps;

        private InputStreamReader is;

        public void doIt0() throws IOException {
            is = new  InputStreamReader(inps);
        }

        private StringBuilder sb;

        private BufferedReader br;

        private String read;

        private String theString;

        public void doIt1() throws IOException {
            sb = new  StringBuilder();
            br = new  BufferedReader(is);
            read = br.readLine();
            while (read != null) {
                sb.append(read);
                read = br.readLine();
            }
            theString = sb.toString().toUpperCase();
        }

        private EnigmaMachine machine;

        public void doIt2() throws IOException {
            machine = EnigmaFactory.buildEnigmaMachine();
        }

        public void doIt3() throws IOException {
            // Set the rotors, encrypt the string and print the results
            machine.setRotors(5, 9, 14);
        }

        private String encodedString;

        private String name;

        private String value;

        public void doIt4() throws IOException {
            encodedString = machine.encodeLine(theString);
            name = "Enigma transformation (5, 9, 14)";
            value = encodedString;
        }

        public TCResult doIt5() throws IOException {
            return new  TCResult(name, value);
        }
    }
}
