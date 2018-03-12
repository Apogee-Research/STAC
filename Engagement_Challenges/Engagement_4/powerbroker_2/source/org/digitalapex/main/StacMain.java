package org.digitalapex.main;

import org.digitalapex.talkers.TalkersIdentity;
import org.digitalapex.powerbroker.CommodityGoBetween;
import org.digitalapex.powerbroker.CommodityGoBetweenControl;
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
        CommandLineParser grabber = new DefaultParser();
        options.addOption("i", true, "Path to the id file for this powerbroker instance");

        try {
            TalkersIdentity identity = null;

            CommandLine cmd = grabber.parse(options, args);
            if (cmd.hasOption("i")) {
                String id = cmd.getOptionValue("i");
                identity = TalkersIdentity.loadFromFile(new File(id));
                System.err.println("Loaded id: " + identity);
            } else {
                mainWorker(options);
            }

            CommodityGoBetween commodityGoBetween = new CommodityGoBetween(identity);
            CommodityGoBetweenControl control = new CommodityGoBetweenControl(commodityGoBetween);

            commodityGoBetween.defineCommodityGoBetweenUser(control);

            control.run();

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

    private static void mainWorker(Options options) {
        new StacMainUtility(options).invoke();
    }

    private static class StacMainUtility {
        private Options options;

        public StacMainUtility(Options options) {
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
