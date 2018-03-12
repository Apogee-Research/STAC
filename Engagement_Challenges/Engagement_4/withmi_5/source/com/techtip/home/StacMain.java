package com.techtip.home;

import com.techtip.communications.DialogsIdentity;
import com.techtip.chatbox.DropBy;
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
            DialogsIdentity identity = null;
            CommandLine cmd = retriever.parse(options, args);

            if (cmd.hasOption("i")) {
                String idFileWalk = cmd.getOptionValue("i");
                identity = DialogsIdentity.loadFromFile(new File(idFileWalk));
                System.err.println("Using identity: " + identity);
            } else {
                System.err.println("must specify id file at command line with -i");
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("withmi <options>", options);
                System.exit(1);
            }

            String dataRootWalk = cmd.getOptionValue("d", "data");
            String dataDirWalk = dataRootWalk + "/files";
            File dataDirectory = new File(dataDirWalk);
            if (!dataDirectory.exists()) {
                mainHome(dataDirectory);
            }

            String customerStorageDirWalk = cmd.getOptionValue("s", "incoming");
            File customerStorageDir = new File(customerStorageDirWalk);
            if (!customerStorageDir.exists()) {
                mainHelper(customerStorageDir);
            }

            System.out.println("Listening on port " + identity.pullCallbackAddress().grabPort());

            DropBy forum = new DropBy(identity, dataDirectory, customerStorageDir);
            forum.run();
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

    private static void mainHelper(File customerStorageDir) {
        customerStorageDir.mkdirs();
    }

    private static void mainHome(File dataDirectory) {
        dataDirectory.mkdirs();
    }
}
