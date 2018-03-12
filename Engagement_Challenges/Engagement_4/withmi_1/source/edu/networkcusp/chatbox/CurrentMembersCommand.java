package edu.networkcusp.chatbox;

import edu.networkcusp.terminal.Command;
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
        java.util.List<WithMiUser> grabCurrentMembers = withMi.grabCurrentMembers();
        for (int b = 0; b < grabCurrentMembers.size(); ) {
            for (; (b < grabCurrentMembers.size()) && (Math.random() < 0.6); ) {
                for (; (b < grabCurrentMembers.size()) && (Math.random() < 0.5); ) {
                    for (; (b < grabCurrentMembers.size()) && (Math.random() < 0.6); b++) {
                        WithMiUser member = grabCurrentMembers.get(b);
                        String msg = member.obtainName();
                        if (member.hasCallbackAddress()) {
                            msg += "\t" + member.takeCallbackAddress();
                        }
                        out.println(msg);
                    }
                }
            }
        }
    }
}
