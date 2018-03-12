package net.robotictip.dropbys;

import net.robotictip.protocols.SenderReceiversTrouble;
import net.robotictip.display.Command;
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
            new ConnectCommandHelp(out).invoke();
        } else {
            executeHelp(out, argList);
        }
    }

    private void executeHelp(PrintStream out, List<String> argList) {
        String home = argList.get(0);
        String port = argList.get(1);
        try {
            withMi.connect(home, Integer.parseInt(port), false);
        } catch (SenderReceiversTrouble e) {
            out.println("Error connecting: " + e.getMessage());
        }
    }

    private class ConnectCommandHelp {
        private PrintStream out;

        public ConnectCommandHelp(PrintStream out) {
            this.out = out;
        }

        public void invoke() {
            out.println(USAGE);
        }
    }
}
