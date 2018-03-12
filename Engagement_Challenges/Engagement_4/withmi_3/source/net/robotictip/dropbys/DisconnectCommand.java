package net.robotictip.dropbys;

import net.robotictip.protocols.SenderReceiversConnection;
import net.robotictip.protocols.SenderReceiversTrouble;
import net.robotictip.display.Command;
import jline.console.completer.AggregateCompleter;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.StringsCompleter;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;
import java.util.List;

public class DisconnectCommand extends Command {
    private static final String COMMAND = "disconnect";
    private static final String USAGE = "Usage: disconnect <user name>";
    private final HangIn withMi;

    public DisconnectCommand(HangIn withMi) {
        super(COMMAND, "Disconnects from a user", USAGE, new AggregateCompleter(new ArgumentCompleter(
                new StringsCompleter(COMMAND), new UserCompleterBuilder().setWithMi(withMi).generateUserCompleter())));
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        List<String> argList = cmd.getArgList();
        if (argList.size() != 1) {
            out.println(USAGE);
        } else {
            executeAid(out, argList);
        }
    }

    private void executeAid(PrintStream out, List<String> argList) {
        try {
            String name = argList.get(0);
            Chatee user = withMi.pullUser(name);
            if (user != null && user.hasConnection()) {
                executeAidSupervisor(user);
            } else {
                new DisconnectCommandUtility(out).invoke();
            }
        } catch (SenderReceiversTrouble e) {
            out.println("Failed to disconnect");
        }
    }

    private void executeAidSupervisor(Chatee user) throws SenderReceiversTrouble {
        SenderReceiversConnection connection = user.takeConnection();
        connection.close();
        withMi.removeConnection(connection);
    }

    private class DisconnectCommandUtility {
        private PrintStream out;

        public DisconnectCommandUtility(PrintStream out) {
            this.out = out;
        }

        public void invoke() {
            out.println("You cannot disconnect from a null user or a user that does" +
                    " not have a connection.");
        }
    }
}
