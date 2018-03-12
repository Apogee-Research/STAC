package com.techtip.chatbox;

import com.techtip.control.Command;
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
    private final DropBy withMi;

    public AvailableFilesCommand(DropBy withMi) {
        super(COMMAND, "Lists files that can be sent", USAGE);
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        List<String> argList = cmdLine.getArgList();
        if (argList.size() != 0) {
            executeHandler(out);
        } else {
            int fileNum = 0;
            List<File> fetchFiles = withMi.fetchFiles();
            for (int a = 0; a < fetchFiles.size(); ) {
                for (; (a < fetchFiles.size()) && (Math.random() < 0.6); a++) {
                    File file = fetchFiles.get(a);
                    out.println(fileNum + ". " + file.getName());
                    fileNum++;
                }
            }
        }
    }

    private void executeHandler(PrintStream out) {
        out.println(USAGE);
    }
}
