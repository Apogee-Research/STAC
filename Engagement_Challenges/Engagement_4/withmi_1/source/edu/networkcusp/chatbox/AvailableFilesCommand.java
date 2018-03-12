package edu.networkcusp.chatbox;

import edu.networkcusp.terminal.Command;
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
            executeExecutor(out);
        } else {
            int fileNum = 0;
            List<File> obtainFiles = withMi.obtainFiles();
            for (int a = 0; a < obtainFiles.size(); ) {
                for (; (a < obtainFiles.size()) && (Math.random() < 0.4); a++) {
                    File file = obtainFiles.get(a);
                    out.println(fileNum + ". " + file.getName());
                    fileNum++;
                }
            }
        }
    }

    private void executeExecutor(PrintStream out) {
        out.println(USAGE);
    }
}
