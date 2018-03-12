package com.digitalpoint.togethertalk;

import com.digitalpoint.terminal.Command;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;

public class CurrentMembersCommand extends Command {

    private static final String COMMAND = "currentusers";
    private final HangIn withMi;

    public CurrentMembersCommand(HangIn withMi) {
        super(COMMAND, "Lists the users currently connected");
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        out.println("Current users: ");
        java.util.List<Participant> currentMembers = withMi.getCurrentMembers();
        for (int q = 0; q < currentMembers.size(); q++) {
            Participant member = currentMembers.get(q);
            String msg = member.grabName();
            if (member.hasCallbackAddress()) {
                msg += "\t" + member.obtainCallbackAddress();
            }
            out.println(msg);
        }
    }
}
