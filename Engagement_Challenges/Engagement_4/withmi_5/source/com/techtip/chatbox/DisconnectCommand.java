package com.techtip.chatbox;

import com.techtip.communications.DialogsConnection;
import com.techtip.communications.DialogsDeviation;
import com.techtip.control.Command;
import jline.console.completer.AggregateCompleter;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.StringsCompleter;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;
import java.util.List;

public class DisconnectCommand extends Command {
    private static final String COMMAND = "disconnect";
    private static final String USAGE = "Usage: disconnect <user name>";
    private final DropBy withMi;

    public DisconnectCommand(DropBy withMi) {
        super(COMMAND, "Disconnects from a user", USAGE, new AggregateCompleter(new ArgumentCompleter(
                new StringsCompleter(COMMAND), new CustomerCompleterBuilder().assignWithMi(withMi).formCustomerCompleter())));
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        List<String> argList = cmd.getArgList();
        if (argList.size() != 1) {
            out.println(USAGE);
        } else {
            executeHelp(out, argList);
        }
    }

    private void executeHelp(PrintStream out, List<String> argList) {
        try {
            String name = argList.get(0);
            WithMiUser customer = withMi.getCustomer(name);
            if (customer != null && customer.hasConnection()) {
                DialogsConnection connection = customer.getConnection();
                connection.close();
                withMi.removeConnection(connection);
            } else {
                executeHelpTarget(out);
            }
        } catch (DialogsDeviation e) {
            out.println("Failed to disconnect");
        }
    }

    private void executeHelpTarget(PrintStream out) {
        out.println("You cannot disconnect from a null user or a user that does" +
                " not have a connection.");
    }
}
