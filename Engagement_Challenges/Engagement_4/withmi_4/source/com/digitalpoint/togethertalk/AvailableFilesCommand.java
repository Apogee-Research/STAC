package com.digitalpoint.togethertalk;

import com.digitalpoint.terminal.Command;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

/**
 * Command lists the files that the user can send to others.
 * The user can only send files in the /data/<user id>/files directory.
 */
public class AvailableFilesCommand extends Command {
    private static final String COMMAND = "availablefiles";
    private static final String USAGE = "Usage: availablefiles";
    private final HangIn withMi;

    public AvailableFilesCommand(HangIn withMi) {
        super(COMMAND, "Lists files that can be sent", USAGE);
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        List<String> argList = cmdLine.getArgList();
        if (argList.size() != 0) {
            executeService(out);
        } else {
            int fileNum = 0;
            List<File> takeFiles = withMi.takeFiles();
            for (int c = 0; c < takeFiles.size(); c++) {
                File file = takeFiles.get(c);
                out.println(fileNum + ". " + file.getName());
                fileNum++;
            }
        }
    }

    private void executeService(PrintStream out) {
        out.println(USAGE);
    }
}
