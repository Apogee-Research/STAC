package edu.networkcusp.chatbox;

import edu.networkcusp.terminal.Command;
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
        java.util.List<WithMiUser> pastMembers = withMi.getPastMembers();
        for (int c = 0; c < pastMembers.size(); c++) {
            WithMiUser member = pastMembers.get(c);
            if (member != null) {
                String msg = member.obtainName();
                if (member.hasCallbackAddress()) {
                    msg += "\t" + member.takeCallbackAddress();
                }
                out.println(msg);
            }
        }
    }
}

