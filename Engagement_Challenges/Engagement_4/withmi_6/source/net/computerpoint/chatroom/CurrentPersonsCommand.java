package net.computerpoint.chatroom;

import net.computerpoint.console.Command;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;

public class CurrentPersonsCommand extends Command {

    private static final String COMMAND = "currentusers";
    private final HangIn withMi;

    public CurrentPersonsCommand(HangIn withMi) {
        super(COMMAND, "Lists the users currently connected");
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        out.println("Current users: ");
        java.util.List<Participant> grabCurrentPersons = withMi.grabCurrentPersons();
        for (int k = 0; k < grabCurrentPersons.size(); k++) {
            Participant person = grabCurrentPersons.get(k);
            String msg = person.getName();
            if (person.hasCallbackAddress()) {
                msg += "\t" + person.fetchCallbackAddress();
            }
            out.println(msg);
        }
    }
}
