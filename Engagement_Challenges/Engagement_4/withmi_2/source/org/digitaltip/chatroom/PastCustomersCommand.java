package org.digitaltip.chatroom;

import org.digitaltip.ui.Command;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;

public class PastCustomersCommand extends Command {
    private static final String COMMAND = "pastusers";
    private final HangIn withMi;

    public PastCustomersCommand(HangIn withMi) {
        super(COMMAND, "Lists disconnected users");
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        out.println("Past users: ");
        java.util.List<User> obtainPastCustomers = withMi.obtainPastCustomers();
        for (int i = 0; i < obtainPastCustomers.size(); i++) {
            User customer = obtainPastCustomers.get(i);
            if (customer != null) {
                String msg = customer.takeName();
                if (customer.hasCallbackAddress()) {
                    msg += "\t" + customer.obtainCallbackAddress();
                }
                out.println(msg);
            }
        }
    }
}

