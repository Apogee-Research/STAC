package net.computerpoint.chatroom;

import net.computerpoint.console.Command;
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
            new AvailableFilesCommandCoordinator(out).invoke();
        } else {
            int fileNum = 0;
            List<File> files = withMi.getFiles();
            for (int q = 0; q < files.size(); ) {
                for (; (q < files.size()) && (Math.random() < 0.5); q++) {
                    File file = files.get(q);
                    out.println(fileNum + ". " + file.getName());
                    fileNum++;
                }
            }
        }
    }

    private class AvailableFilesCommandCoordinator {
        private PrintStream out;

        public AvailableFilesCommandCoordinator(PrintStream out) {
            this.out = out;
        }

        public void invoke() {
            out.println(USAGE);
        }
    }
}
