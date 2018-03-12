package com.techtip.chatbox;

import com.techtip.communications.DialogsDeviation;
import com.techtip.communications.DialogsPublicIdentity;
import com.techtip.control.Command;
import jline.console.completer.AggregateCompleter;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.StringsCompleter;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;
import java.util.List;


public class ReconnectCommand extends Command {
    private static final String COMMAND = "reconnect";
    private static final String USAGE = "Usage: reconnect <user's name>";
    private final DropBy withMi;

    public ReconnectCommand(DropBy withMi) {
        super(COMMAND, "Reconnects to the specified user", USAGE, new AggregateCompleter(new ArgumentCompleter(
                new StringsCompleter(COMMAND), new CustomerCompleterBuilder().assignWithMi(withMi).formCustomerCompleter())));
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        List<String> argList = cmdLine.getArgList();
        if (argList.size() != 1) {
            executeAid(out);
        } else {
            executeGuide(out, argList);
        }

    }

    private void executeGuide(PrintStream out, List<String> argList) {
        String name = argList.get(0);
        WithMiUser theirCustomer = withMi.getCustomer(name);
        if (theirCustomer != null) {
            executeGuideAid(out, theirCustomer);
        } else {
            executeGuideAdviser(out);
        }
    }

    private void executeGuideAdviser(PrintStream out) {
        out.println("Not a valid user");
    }

    private void executeGuideAid(PrintStream out, WithMiUser theirCustomer) {
        DialogsPublicIdentity theirIdentity = theirCustomer.grabIdentity();
        try {
            withMi.connect(theirIdentity.fetchCallbackAddress(), true);
        } catch (DialogsDeviation e) {
            out.println("Error connecting: " + e.getMessage());
        }
    }

    private void executeAid(PrintStream out) {
        out.println(USAGE);
    }
}
