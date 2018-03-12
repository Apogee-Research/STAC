package com.virtualpoint.start;

import com.virtualpoint.talkers.DialogsIdentity;
import com.virtualpoint.broker.ProductIntermediary;
import com.virtualpoint.broker.ProductIntermediaryDisplay;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;

public class StacMain {

    public static void main(String[] args) {
        Options options = new Options();
        CommandLineParser retriever = new DefaultParser();
        options.addOption("i", true, "Path to the id file for this powerbroker instance");

        try {
            DialogsIdentity identity = null;

            CommandLine cmd = retriever.parse(options, args);
            if (cmd.hasOption("i")) {
                String id = cmd.getOptionValue("i");
                identity = DialogsIdentity.loadFromFile(new File(id));
                System.err.println("Loaded id: " + identity);
            } else {
                System.err.println("must specify id file -i");
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("powerbroker <options>", options);
                System.exit(1);
            }

            ProductIntermediary productIntermediary = new ProductIntermediary(identity);
            ProductIntermediaryDisplay display = new ProductIntermediaryDisplay(productIntermediary);

            productIntermediary.setProductIntermediaryUser(display);

            display.run();

        } catch (ParseException ex) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("powerbroker <options>", options);
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + ex.getMessage());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
