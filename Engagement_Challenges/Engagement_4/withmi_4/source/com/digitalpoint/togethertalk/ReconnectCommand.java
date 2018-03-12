package com.digitalpoint.togethertalk;

import com.digitalpoint.dialogs.SenderReceiversException;
import com.digitalpoint.dialogs.SenderReceiversPublicIdentity;
import com.digitalpoint.terminal.Command;
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
                new StringsCompleter(COMMAND), new MemberCompleterBuilder().assignWithMi(withMi).makeMemberCompleter())));
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        List<String> argList = cmdLine.getArgList();
        if (argList.size() != 1) {
            out.println(USAGE);
        } else {
            String name = argList.get(0);
            Participant theirMember = withMi.getMember(name);
            if (theirMember != null) {
                SenderReceiversPublicIdentity theirIdentity = theirMember.getIdentity();
                try {
                    withMi.connect(theirIdentity.fetchCallbackAddress(), true);
                } catch (SenderReceiversException e) {
                    out.println("Error connecting: " + e.getMessage());
                }
            } else {
                executeUtility(out);
            }
        }

    }

    private void executeUtility(PrintStream out) {
        new ReconnectCommandHelper(out).invoke();
    }

    private class ReconnectCommandHelper {
        private PrintStream out;

        public ReconnectCommandHelper(PrintStream out) {
            this.out = out;
        }

        public void invoke() {
            out.println("Not a valid user");
        }
    }
}
