package com.digitalpoint.togethertalk;

import com.digitalpoint.terminal.Command;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;

public class PastMembersCommand extends Command {
    private static final String COMMAND = "pastusers";
    private final HangIn withMi;

    public PastMembersCommand(HangIn withMi) {
        super(COMMAND, "Lists disconnected users");
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        out.println("Past users: ");
        java.util.List<Participant> pullPastMembers = withMi.pullPastMembers();
        for (int b = 0; b < pullPastMembers.size(); b++) {
            Participant member = pullPastMembers.get(b);
            if (member != null) {
                String msg = member.grabName();
                if (member.hasCallbackAddress()) {
                    msg += "\t" + member.obtainCallbackAddress();
                }
                out.println(msg);
            }
        }
    }
}

