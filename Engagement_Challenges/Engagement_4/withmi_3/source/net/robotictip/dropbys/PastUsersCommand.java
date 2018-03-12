package net.robotictip.dropbys;

import net.robotictip.display.Command;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;

public class PastUsersCommand extends Command {
    private static final String COMMAND = "pastusers";
    private final HangIn withMi;

    public PastUsersCommand(HangIn withMi) {
        super(COMMAND, "Lists disconnected users");
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        out.println("Past users: ");
        java.util.List<Chatee> fetchPastUsers = withMi.fetchPastUsers();
        for (int i = 0; i < fetchPastUsers.size(); i++) {
            Chatee user = fetchPastUsers.get(i);
            if (user != null) {
                String msg = user.obtainName();
                if (user.hasCallbackAddress()) {
                    msg += "\t" + user.fetchCallbackAddress();
                }
                out.println(msg);
            }
        }
    }
}

