package org.digitaltip.chatroom;

import org.digitaltip.dialogs.TalkersConnection;
import org.digitaltip.dialogs.TalkersDeviation;
import org.digitaltip.ui.Command;
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
                new StringsCompleter(COMMAND), new CustomerCompleter(withMi))));
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        List<String> argList = cmd.getArgList();
        if (argList.size() != 1) {
            out.println(USAGE);
        } else {
            try {
                String name = argList.get(0);
                User customer = withMi.pullCustomer(name);
                if (customer != null && customer.hasConnection()) {
                    executeUtility(customer);
                } else {
                    out.println("You cannot disconnect from a null user or a user that does" +
                            " not have a connection.");
                }
            } catch (TalkersDeviation e) {
                out.println("Failed to disconnect");
            }
        }
    }

    private void executeUtility(User customer) throws TalkersDeviation {
        new DisconnectCommandGuide(customer).invoke();
    }

    private class DisconnectCommandGuide {
        private User customer;

        public DisconnectCommandGuide(User customer) {
            this.customer = customer;
        }

        public void invoke() throws TalkersDeviation {
            TalkersConnection connection = customer.grabConnection();
            connection.close();
            withMi.removeConnection(connection);
        }
    }
}
