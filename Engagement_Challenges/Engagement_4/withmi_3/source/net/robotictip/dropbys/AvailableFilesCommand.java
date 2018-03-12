package net.robotictip.dropbys;

import net.robotictip.display.Command;
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
            executeGuide(out);
        } else {
            int fileNum = 0;
            List<File> grabFiles = withMi.grabFiles();
            for (int i = 0; i < grabFiles.size(); i++) {
                File file = grabFiles.get(i);
                out.println(fileNum + ". " + file.getName());
                fileNum++;
            }
        }
    }

    private void executeGuide(PrintStream out) {
        out.println(USAGE);
    }
}
