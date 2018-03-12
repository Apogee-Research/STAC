package com.digitalpoint.togethertalk;

import com.digitalpoint.dialogs.SenderReceiversConnection;
import com.digitalpoint.dialogs.SenderReceiversException;
import com.digitalpoint.terminal.Command;
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
                new StringsCompleter(COMMAND), new MemberCompleterBuilder().assignWithMi(withMi).makeMemberCompleter())));
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        List<String> argList = cmd.getArgList();
        if (argList.size() != 1) {
            executeEntity(out);
        } else {
            try {
                String name = argList.get(0);
                Participant member = withMi.getMember(name);
                if (member != null && member.hasConnection()) {
                    new DisconnectCommandService(member).invoke();
                } else {
                    out.println("You cannot disconnect from a null user or a user that does" +
                            " not have a connection.");
                }
            } catch (SenderReceiversException e) {
                out.println("Failed to disconnect");
            }
        }
    }

    private void executeEntity(PrintStream out) {
        out.println(USAGE);
    }

    private class DisconnectCommandService {
        private Participant member;

        public DisconnectCommandService(Participant member) {
            this.member = member;
        }

        public void invoke() throws SenderReceiversException {
            SenderReceiversConnection connection = member.takeConnection();
            connection.close();
            withMi.removeConnection(connection);
        }
    }
}
