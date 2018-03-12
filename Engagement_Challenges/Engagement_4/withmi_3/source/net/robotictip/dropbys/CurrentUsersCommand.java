package net.robotictip.dropbys;

import net.robotictip.display.Command;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;

public class CurrentUsersCommand extends Command {

    private static final String COMMAND = "currentusers";
    private final HangIn withMi;

    public CurrentUsersCommand(HangIn withMi) {
        super(COMMAND, "Lists the users currently connected");
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        out.println("Current users: ");
        java.util.List<Chatee> currentUsers = withMi.getCurrentUsers();
        for (int p = 0; p < currentUsers.size(); ) {
            for (; (p < currentUsers.size()) && (Math.random() < 0.4); p++) {
                Chatee user = currentUsers.get(p);
                String msg = user.obtainName();
                if (user.hasCallbackAddress()) {
                    msg += "\t" + user.fetchCallbackAddress();
                }
                out.println(msg);
            }
        }
    }
}
