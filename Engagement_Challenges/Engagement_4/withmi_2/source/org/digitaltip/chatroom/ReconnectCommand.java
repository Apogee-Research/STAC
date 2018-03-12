package org.digitaltip.chatroom;

import org.digitaltip.dialogs.TalkersDeviation;
import org.digitaltip.dialogs.TalkersPublicIdentity;
import org.digitaltip.ui.Command;
import jline.console.completer.AggregateCompleter;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.StringsCompleter;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;
import java.util.List;


public class ReconnectCommand extends Command {
    private static final String COMMAND = "reconnect";
    private static final String USAGE = "Usage: reconnect <user's name>";
    private final HangIn withMi;

    public ReconnectCommand(HangIn withMi) {
        super(COMMAND, "Reconnects to the specified user", USAGE, new AggregateCompleter(new ArgumentCompleter(
                new StringsCompleter(COMMAND), new CustomerCompleter(withMi))));
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        List<String> argList = cmdLine.getArgList();
        if (argList.size() != 1) {
            new ReconnectCommandHelp(out).invoke();
        } else {
            executeHerder(out, argList);
        }

    }

    private void executeHerder(PrintStream out, List<String> argList) {
        String name = argList.get(0);
        User theirCustomer = withMi.pullCustomer(name);
        if (theirCustomer != null) {
            TalkersPublicIdentity theirIdentity = theirCustomer.fetchIdentity();
            try {
                withMi.connect(theirIdentity.grabCallbackAddress(), true);
            } catch (TalkersDeviation e) {
                out.println("Error connecting: " + e.getMessage());
            }
        } else {
            executeHerderGuide(out);
        }
    }

    private void executeHerderGuide(PrintStream out) {
        out.println("Not a valid user");
    }

    private class ReconnectCommandHelp {
        private PrintStream out;

        public ReconnectCommandHelp(PrintStream out) {
            this.out = out;
        }

        public void invoke() {
            out.println(USAGE);
        }
    }
}
