package net.computerpoint.chatroom;

import net.computerpoint.dialogs.ProtocolsConnection;
import net.computerpoint.dialogs.ProtocolsDeviation;
import net.computerpoint.console.Command;
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
                new StringsCompleter(COMMAND), new PersonCompleter(withMi))));
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        List<String> argList = cmd.getArgList();
        if (argList.size() != 1) {
            out.println(USAGE);
        } else {
            try {
                String name = argList.get(0);
                Participant person = withMi.obtainPerson(name);
                if (person != null && person.hasConnection()) {
                    executeAdviser(person);
                } else {
                    out.println("You cannot disconnect from a null user or a user that does" +
                            " not have a connection.");
                }
            } catch (ProtocolsDeviation e) {
                out.println("Failed to disconnect");
            }
        }
    }

    private void executeAdviser(Participant person) throws ProtocolsDeviation {
        ProtocolsConnection connection = person.takeConnection();
        connection.close();
        withMi.removeConnection(connection);
    }
}
