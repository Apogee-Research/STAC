package net.robotictip.dropbys;

import net.robotictip.protocols.SenderReceiversTrouble;
import net.robotictip.protocols.SenderReceiversPublicIdentity;
import net.robotictip.display.Command;
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
                new StringsCompleter(COMMAND), new UserCompleterBuilder().setWithMi(withMi).generateUserCompleter())));
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        List<String> argList = cmdLine.getArgList();
        if (argList.size() != 1) {
            out.println(USAGE);
        } else {
            String name = argList.get(0);
            Chatee theirUser = withMi.pullUser(name);
            if (theirUser != null) {
                executeGuide(out, theirUser);
            } else {
                executeAdviser(out);
            }
        }

    }

    private void executeAdviser(PrintStream out) {
        out.println("Not a valid user");
    }

    private void executeGuide(PrintStream out, Chatee theirUser) {
        SenderReceiversPublicIdentity theirIdentity = theirUser.pullIdentity();
        try {
            withMi.connect(theirIdentity.getCallbackAddress(), true);
        } catch (SenderReceiversTrouble e) {
            out.println("Error connecting: " + e.getMessage());
        }
    }
}
