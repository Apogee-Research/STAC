package com.techtip.chatbox;

import com.techtip.control.Command;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;

public class CurrentCustomersCommand extends Command {

    private static final String COMMAND = "currentusers";
    private final DropBy withMi;

    public CurrentCustomersCommand(DropBy withMi) {
        super(COMMAND, "Lists the users currently connected");
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        out.println("Current users: ");
        java.util.List<WithMiUser> grabCurrentCustomers = withMi.grabCurrentCustomers();
        for (int p = 0; p < grabCurrentCustomers.size(); p++) {
            WithMiUser customer = grabCurrentCustomers.get(p);
            String msg = customer.pullName();
            if (customer.hasCallbackAddress()) {
                msg += "\t" + customer.obtainCallbackAddress();
            }
            out.println(msg);
        }
    }
}
