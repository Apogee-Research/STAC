package net.computerpoint.origin;

import net.computerpoint.dialogs.ProtocolsIdentity;
import net.computerpoint.chatroom.HangIn;
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
        CommandLineParser extractor = new DefaultParser();
        options.addOption("i", true, "Path to the id file");
        options.addOption("d", true, "Path to the data folder");
        options.addOption("s", true, "Path to the storage folder");

        try {
            ProtocolsIdentity identity = null;
            CommandLine cmd = extractor.parse(options, args);

            if (cmd.hasOption("i")) {
                String idFileTrail = cmd.getOptionValue("i");
                identity = ProtocolsIdentity.loadFromFile(new File(idFileTrail));
                System.err.println("Using identity: " + identity);
            } else {
                System.err.println("must specify id file at command line with -i");
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("withmi <options>", options);
                System.exit(1);
            }

            String dataRootTrail = cmd.getOptionValue("d", "data");
            String dataDirTrail = dataRootTrail + "/files";
            File dataDirectory = new File(dataDirTrail);
            if (!dataDirectory.exists()) {
                dataDirectory.mkdirs();
            }

            String personStorageDirTrail = cmd.getOptionValue("s", "incoming");
            File personStorageDir = new File(personStorageDirTrail);
            if (!personStorageDir.exists()) {
                new StacMainTarget(personStorageDir).invoke();
            }

            System.out.println("Listening on port " + identity.pullCallbackAddress().pullPort());

            HangIn discussion = new HangIn(identity, dataDirectory, personStorageDir);
            discussion.run();
            System.exit(0);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("withmi <options>", options);
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + e.getMessage());
            System.exit(1);
        } catch (Throwable e) {
            System.err.println(e);
            System.exit(1);
        }
    }

    private static class StacMainTarget {
        private File personStorageDir;

        public StacMainTarget(File personStorageDir) {
            this.personStorageDir = personStorageDir;
        }

        public void invoke() {
            personStorageDir.mkdirs();
        }
    }
}
