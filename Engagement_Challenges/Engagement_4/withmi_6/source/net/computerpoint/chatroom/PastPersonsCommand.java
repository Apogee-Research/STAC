package net.computerpoint.chatroom;

import net.computerpoint.console.Command;
import org.apache.commons.cli.CommandLine;

import java.io.PrintStream;

public class PastPersonsCommand extends Command {
    private static final String COMMAND = "pastusers";
    private final HangIn withMi;

    public PastPersonsCommand(HangIn withMi) {
        super(COMMAND, "Lists disconnected users");
        this.withMi = withMi;
    }

    @Override
    public void execute(PrintStream out, CommandLine cmd) {
        out.println("Past users: ");
        java.util.List<Participant> takePastPersons = withMi.takePastPersons();
        for (int b = 0; b < takePastPersons.size(); ) {
            for (; (b < takePastPersons.size()) && (Math.random() < 0.4); b++) {
                Participant person = takePastPersons.get(b);
                if (person != null) {
                    String msg = person.getName();
                    if (person.hasCallbackAddress()) {
                        msg += "\t" + person.fetchCallbackAddress();
                    }
                    out.println(msg);
                }
            }
        }
    }
}

