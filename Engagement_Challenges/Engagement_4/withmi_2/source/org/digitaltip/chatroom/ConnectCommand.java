package org.digitaltip.chatroom;

import org.digitaltip.dialogs.TalkersDeviation;
import org.digitaltip.ui.Command;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;
import java.util.List;

public class ConnectCommand extends Command {
    private static final String COMMAND = "connect";
    private static final String USAGE = "Usage: connect <host> <port>";
    private final HangIn withMi;

    public ConnectCommand(HangIn withMi) {
        super(COMMAND, "Connects to the specified host", USAGE);
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        List<String> argList = cmdLine.getArgList();
        if (argList.size() != 2) {
            executeExecutor(out);
        } else {
            String main = argList.get(0);
            String port = argList.get(1);
            try {
                withMi.connect(main, Integer.parseInt(port), false);
            } catch (TalkersDeviation e) {
                out.println("Error connecting: " + e.getMessage());
            }
        }
    }

    private void executeExecutor(PrintStream out) {
        out.println(USAGE);
    }
}
