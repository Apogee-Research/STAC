/*
 * Textcrunchr main
 */
package com.cyberpointllc.stac.host;

import java.io.IOException;
import java.util.List;
import com.cyberpointllc.stac.textcrunchr.InputPathHandler;
import com.cyberpointllc.stac.textcrunchr.TextFileHandler;
import com.cyberpointllc.stac.textcrunchr.OutputHandler;
import com.cyberpointllc.stac.textcrunchr.OutputHandlerFactory;
import com.cyberpointllc.stac.textcrunchr.OutputHandlerException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;

public class Main {

    /*
     * main
     */
    public static void main(String[] args) {
        mainHelper(args);
    }

    private static void mainHelper(String[] args) {
        Options options = new  Options();
        String filename = "";
        String outputOption = "";
        String[] processors = new String[0];
        OutputHandler outph;
        try {
            CommandLineParser parser = new  DefaultParser();
            options.addOption("o", true, "Output form. Defaults to console." + "\nUse either \"console\", \"xml\" or \"window\"");
            options.addOption("h", false, "Display this help message");
            Option processOption = new  Option("p", "Processors. Defaults to running all Processors." + "\nPick from \"characterCount\", \"enigma\", \"languageAnalysis\", \"sentenceStats\", \"wordStats\", \"wordFreqs\"");
            processOption.setArgs(Option.UNLIMITED_VALUES);
            options.addOption(processOption);
            InputPathHandler ipHandler = new  InputPathHandler();
            TextFileHandler tfHandler = new  TextFileHandler();
            try {
                CommandLine cmd = parser.parse(options, args);
                if (cmd.hasOption("p")) {
                    processors = cmd.getOptionValues("p");
                }
                if (cmd.hasOption("o")) {
                    outputOption = cmd.getOptionValue("o");
                } else if (cmd.hasOption("h")) {
                    HelpFormatter formatter = new  HelpFormatter();
                    formatter.printHelp("TextCrunchr <options> <file>", options);
                    System.exit(0);
                }
                filename = cmd.getArgs()[0];
            } catch (ParseException exp) {
                System.err.println("Parsing failed.  Reason: " + exp.getMessage());
                System.exit(1);
            }
            List<String> files = ipHandler.handleInputPath(filename);
            outph = OutputHandlerFactory.getOutputHandler(outputOption);
            for (String filepath : files) {
                try {
                    tfHandler.processFile(filepath, outph, processors);
                } catch (IOException ioe) {
                    System.out.println("file " + filepath + " could not be analyzed.");
                    ioe.printStackTrace();
                }
            }
            // conclude and output
            try {
                outph.conclude();
            } catch (OutputHandlerException ohe) {
                System.out.println(ohe.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("TextCrunchr has crashed.");
        }
    }
}
