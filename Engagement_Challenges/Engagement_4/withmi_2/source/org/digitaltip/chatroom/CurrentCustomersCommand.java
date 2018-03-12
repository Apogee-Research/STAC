package org.digitaltip.chatroom;

import org.digitaltip.ui.Command;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;

public class CurrentCustomersCommand extends Command {

    private static final String COMMAND = "currentusers";
    private final HangIn withMi;

    public CurrentCustomersCommand(HangIn withMi) {
        super(COMMAND, "Lists the users currently connected");
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        out.println("Current users: ");
        java.util.List<User> fetchCurrentCustomers = withMi.fetchCurrentCustomers();
        for (int j = 0; j < fetchCurrentCustomers.size(); j++) {
            User customer = fetchCurrentCustomers.get(j);
            String msg = customer.takeName();
            if (customer.hasCallbackAddress()) {
                msg += "\t" + customer.obtainCallbackAddress();
            }
            out.println(msg);
        }
    }
}
