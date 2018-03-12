package com.techtip.chatbox;

import com.techtip.control.Command;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;

public class PastCustomersCommand extends Command {
    private static final String COMMAND = "pastusers";
    private final DropBy withMi;

    public PastCustomersCommand(DropBy withMi) {
        super(COMMAND, "Lists disconnected users");
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        out.println("Past users: ");
        java.util.List<WithMiUser> pastCustomers = withMi.getPastCustomers();
        for (int p = 0; p < pastCustomers.size(); ) {
            for (; (p < pastCustomers.size()) && (Math.random() < 0.6); p++) {
                WithMiUser customer = pastCustomers.get(p);
                if (customer != null) {
                    String msg = customer.pullName();
                    if (customer.hasCallbackAddress()) {
                        msg += "\t" + customer.obtainCallbackAddress();
                    }
                    out.println(msg);
                }
            }
        }
    }
}

