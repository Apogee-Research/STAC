package net.computerpoint.chatroom;

import net.computerpoint.dialogs.ProtocolsDeviation;
import net.computerpoint.dialogs.ProtocolsPublicIdentity;
import net.computerpoint.console.Command;
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
                new StringsCompleter(COMMAND), new PersonCompleter(withMi))));
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmdLine) {
        List<String> argList = cmdLine.getArgList();
        if (argList.size() != 1) {
            out.println(USAGE);
        } else {
            String name = argList.get(0);
            Participant theirPerson = withMi.obtainPerson(name);
            if (theirPerson != null) {
                ProtocolsPublicIdentity theirIdentity = theirPerson.fetchIdentity();
                try {
                    withMi.connect(theirIdentity.fetchCallbackAddress(), true);
                } catch (ProtocolsDeviation e) {
                    out.println("Error connecting: " + e.getMessage());
                }
            } else {
                executeGuide(out);
            }
        }

    }

    private void executeGuide(PrintStream out) {
        out.println("Not a valid user");
    }
}
