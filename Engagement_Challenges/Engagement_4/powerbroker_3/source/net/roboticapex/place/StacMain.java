package net.roboticapex.place;

import net.roboticapex.senderReceivers.SenderReceiversIdentity;
import net.roboticapex.broker.ProductLiaison;
import net.roboticapex.broker.ProductLiaisonDisplay;
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
            SenderReceiversIdentity identity = null;

            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("i")) {
                String id = cmd.getOptionValue("i");
                identity = SenderReceiversIdentity.loadFromFile(new File(id));
                System.err.println("Loaded id: " + identity);
            } else {
                new StacMainHerder(options).invoke();
            }

            ProductLiaison productLiaison = new ProductLiaison(identity);
            ProductLiaisonDisplay display = new ProductLiaisonDisplay(productLiaison);

            productLiaison.fixProductLiaisonUser(display);

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

    private static class StacMainHerder {
        private Options options;

        public StacMainHerder(Options options) {
            this.options = options;
        }

        public void invoke() {
            System.err.println("must specify id file -i");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("powerbroker <options>", options);
            System.exit(1);
        }
    }
}
