package edu.networkcusp.main;

import edu.networkcusp.senderReceivers.ProtocolsIdentity;
import edu.networkcusp.broker.ProductIntermediary;
import edu.networkcusp.broker.ProductIntermediaryConsole;
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
        CommandLineParser parser = new DefaultParser();
        options.addOption("i", true, "Path to the id file for this powerbroker instance");

        try {
            ProtocolsIdentity identity = null;

            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("i")) {
                String id = cmd.getOptionValue("i");
                identity = ProtocolsIdentity.loadFromFile(new File(id));
                System.err.println("Loaded id: " + identity);
            } else {
                System.err.println("must specify id file -i");
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("powerbroker <options>", options);
                System.exit(1);
            }

            ProductIntermediary productIntermediary = new ProductIntermediary(identity);
            ProductIntermediaryConsole console = new ProductIntermediaryConsole(productIntermediary);

            productIntermediary.assignProductIntermediaryCustomer(console);

            console.run();

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
