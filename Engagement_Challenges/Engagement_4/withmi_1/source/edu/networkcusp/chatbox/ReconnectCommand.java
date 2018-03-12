package edu.networkcusp.chatbox;

import edu.networkcusp.protocols.CommunicationsFailure;
import edu.networkcusp.protocols.CommunicationsPublicIdentity;
import edu.networkcusp.terminal.Command;
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
                new StringsCompleter(COMMAND), new MemberCompleter(withMi))));
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        List<String> argList = cmdLine.getArgList();
        if (argList.size() != 1) {
            out.println(USAGE);
        } else {
            new ReconnectCommandGuide(out, argList).invoke();
        }

    }

    private class ReconnectCommandGuide {
        private PrintStream out;
        private List<String> argList;

        public ReconnectCommandGuide(PrintStream out, List<String> argList) {
            this.out = out;
            this.argList = argList;
        }

        public void invoke() {
            String name = argList.get(0);
            WithMiUser theirMember = withMi.fetchMember(name);
            if (theirMember != null) {
                invokeEngine(theirMember);
            } else {
                out.println("Not a valid user");
            }
        }

        private void invokeEngine(WithMiUser theirMember) {
            CommunicationsPublicIdentity theirIdentity = theirMember.getIdentity();
            try {
                withMi.connect(theirIdentity.obtainCallbackAddress(), true);
            } catch (CommunicationsFailure e) {
                out.println("Error connecting: " + e.getMessage());
            }
        }
    }
}
