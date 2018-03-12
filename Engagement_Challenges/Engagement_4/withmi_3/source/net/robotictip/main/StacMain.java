package net.robotictip.main;

import net.robotictip.protocols.SenderReceiversIdentity;
import net.robotictip.dropbys.HangIn;
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
        CommandLineParser reader = new DefaultParser();
        options.addOption("i", true, "Path to the id file");
        options.addOption("d", true, "Path to the data folder");
        options.addOption("s", true, "Path to the storage folder");

        try {
            SenderReceiversIdentity identity = null;
            CommandLine cmd = reader.parse(options, args);

            if (cmd.hasOption("i")) {
                String idFilePath = cmd.getOptionValue("i");
                identity = SenderReceiversIdentity.loadFromFile(new File(idFilePath));
                System.err.println("Using identity: " + identity);
            } else {
                System.err.println("must specify id file at command line with -i");
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("withmi <options>", options);
                System.exit(1);
            }

            String dataRootPath = cmd.getOptionValue("d", "data");
            String dataDirPath = dataRootPath + "/files";
            File dataDirectory = new File(dataDirPath);
            if (!dataDirectory.exists()) {
                dataDirectory.mkdirs();
            }

            String userStorageDirPath = cmd.getOptionValue("s", "incoming");
            File userStorageDir = new File(userStorageDirPath);
            if (!userStorageDir.exists()) {
                userStorageDir.mkdirs();
            }

            System.out.println("Listening on port " + identity.takeCallbackAddress().pullPort());

            HangIn discussion = new HangIn(identity, dataDirectory, userStorageDir);
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
}
