package com.digitalpoint.host;

import com.digitalpoint.dialogs.SenderReceiversIdentity;
import com.digitalpoint.togethertalk.HangIn;
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
        options.addOption("i", true, "Path to the id file");
        options.addOption("d", true, "Path to the data folder");
        options.addOption("s", true, "Path to the storage folder");

        try {
            SenderReceiversIdentity identity = null;
            CommandLine cmd = retriever.parse(options, args);

            if (cmd.hasOption("i")) {
                String idFilePath = cmd.getOptionValue("i");
                identity = SenderReceiversIdentity.loadFromFile(new File(idFilePath));
                System.err.println("Using identity: " + identity);
            } else {
                mainGuide(options);
            }

            String dataRootPath = cmd.getOptionValue("d", "data");
            String dataDirPath = dataRootPath + "/files";
            File dataDirectory = new File(dataDirPath);
            if (!dataDirectory.exists()) {
                mainAid(dataDirectory);
            }

            String memberStorageDirPath = cmd.getOptionValue("s", "incoming");
            File memberStorageDir = new File(memberStorageDirPath);
            if (!memberStorageDir.exists()) {
                memberStorageDir.mkdirs();
            }

            System.out.println("Listening on port " + identity.fetchCallbackAddress().pullPort());

            HangIn conference = new HangIn(identity, dataDirectory, memberStorageDir);
            conference.run();
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

    private static void mainAid(File dataDirectory) {
        dataDirectory.mkdirs();
    }

    private static void mainGuide(Options options) {
        System.err.println("must specify id file at command line with -i");
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("withmi <options>", options);
        System.exit(1);
    }
}
