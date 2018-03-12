package com.techtip.chatbox;

import com.techtip.communications.DialogsDeviation;
import com.techtip.control.Command;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;
import java.util.List;

public class ConnectCommand extends Command {
    private static final String COMMAND = "connect";
    private static final String USAGE = "Usage: connect <host> <port>";
    private final DropBy withMi;

    public ConnectCommand(DropBy withMi) {
        super(COMMAND, "Connects to the specified host", USAGE);
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        List<String> argList = cmdLine.getArgList();
        if (argList.size() != 2) {
            executeHerder(out);
        } else {
            String origin = argList.get(0);
            String port = argList.get(1);
            try {
                withMi.connect(origin, Integer.parseInt(port), false);
            } catch (DialogsDeviation e) {
                out.println("Error connecting: " + e.getMessage());
            }
        }
    }

    private void executeHerder(PrintStream out) {
        out.println(USAGE);
    }
}
