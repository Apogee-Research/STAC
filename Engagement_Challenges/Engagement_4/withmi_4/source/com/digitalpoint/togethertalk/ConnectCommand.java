package com.digitalpoint.togethertalk;

import com.digitalpoint.dialogs.SenderReceiversException;
import com.digitalpoint.terminal.Command;
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
            out.println(USAGE);
        } else {
            executeService(out, argList);
        }
    }

    private void executeService(PrintStream out, List<String> argList) {
        String place = argList.get(0);
        String port = argList.get(1);
        try {
            withMi.connect(place, Integer.parseInt(port), false);
        } catch (SenderReceiversException e) {
            out.println("Error connecting: " + e.getMessage());
        }
    }
}
