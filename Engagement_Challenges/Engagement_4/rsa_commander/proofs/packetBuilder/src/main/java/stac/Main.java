package stac;

import stac.communications.Communications;
import stac.crypto.PrivateKey;
import stac.crypto.PublicKey;
import stac.parser.CommandLine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 */
public class Main {
    private static final String MOTD = "SSHC(d) - A secure shell and chat tool for developers.";

    public static void main(String[] args) {
        CommandLine commandLine = configureCommands();
        try {
            CommandLine.Options options = commandLine.parse(args);
            if (options.findByLongOption("good").isSet()) {
                goodMain(options);
            } else if (options.findByLongOption("bad").isSet()){
                badMain(options);
            } else {
                commandLine.throwHelp(null);
            }
        } catch ( CommandLine.ParseHelpfulException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void writePacket(String filename, byte[] bytes) {
        try (FileOutputStream fos = new FileOutputStream(filename)){
            fos.write(bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void goodMain(CommandLine.Options options) {
        PrivateKey privateKey = extractPrivateKey(options);
        PublicKey publicKey = extractPublicKey(options);
        if (privateKey != null && publicKey != null) {
            Communications communications = new Communications(options, null);
            writePacket("firstPacket.bin", communications.getHandshake(privateKey));
            writePacket("secondPacket.bin", communications.getGood(privateKey, publicKey));
            writePacket("thirdPacket.bin", communications.getTermination(privateKey, publicKey));
        }
    }

    private static void badMain(CommandLine.Options options) {
        PrivateKey privateKey = extractPrivateKey(options);
        PublicKey publicKey = extractPublicKey(options);
        if (privateKey != null && publicKey != null) {
            Communications communications = new Communications(options, null);
            writePacket("firstPacket.bin", communications.getHandshake(privateKey));
            writePacket("secondPacket.bin", communications.getBad(privateKey, publicKey));
            writePacket("thirdPacket.bin", communications.getTermination(privateKey, publicKey));
        }
    }

    private static PublicKey extractPublicKey(CommandLine.Options options) {
        PublicKey publicKey;
        try {
            String keyFile = options.findByLongOption("public-key").getValue();
            if (keyFile == null) {
                System.err.println("Missing Public key: --help --public-key for more information.");
                return null;
            } else {
                publicKey = new PublicKey(new File(keyFile));
            }
        } catch (IOException e) {
            System.err.println("**ERROR** Failed to read public-key from: " + options.findByLongOption("public-key"));
            return null;
        }
        return publicKey;
    }

    private static PrivateKey extractPrivateKey(CommandLine.Options options) {
        PrivateKey privateKey;
        try {
            String keyFile = options.findByLongOption("private-key").getValue();
            if (keyFile == null) {
                System.err.println("Missing Private key: --help --private-key for more information.");
                return null;
            } else {
                privateKey = new PrivateKey(new File(keyFile));
            }
        } catch (IOException e) {
            System.err.println("**ERROR** Failed to read private-key from: " + options.findByLongOption("private-key"));
            return null;
        }
        return privateKey;
    }

    private static CommandLine configureCommands() {
        CommandLine commandLine = new CommandLine(MOTD);

        commandLine
            .newOption()
            .longOption("good").longDescription("Build a good packet. (non-malicious)")
            .shortOption("g").shortDescription("non-malicious").hasValue(false, true).done()
            .newOption()
            .longOption("bad").longDescription("Build a bad packet. (malicious)")
            .shortOption("b").shortDescription("malicious").hasValue(false, true).done()
            .newOption()
            .longOption("private-key").longDescription("An unencrypted OpenSSL Private Key File.")
            .shortOption("pr").shortDescription("Private Key file.").hasValue(true, false).done()
            .newOption()
            .longOption("public-key").longDescription("An unencrypted OpenSSL Public Key File.")
            .shortOption("pu").shortDescription("Public Key file.").hasValue(true, false).done();

        return commandLine;
    }


}