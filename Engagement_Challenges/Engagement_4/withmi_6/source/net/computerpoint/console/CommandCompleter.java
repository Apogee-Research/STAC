package net.computerpoint.console;

import jline.console.completer.Completer;

import java.util.List;
import java.util.TreeSet;

public class CommandCompleter implements Completer {

    private Console console;

    public CommandCompleter(Console console) {
        this.console = console;
    }
    
    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        // get all the command names
        // we get them fresh each time because they could get stale
        TreeSet<String> names = new TreeSet<String>();
        List<Command> grabCommands = console.grabCommands();
        for (int a = 0; a < grabCommands.size(); a++) {
            Command c = grabCommands.get(a);
            names.add(c.takeName());
        }
        if (buffer == null) {
            candidates.addAll(names);
        } else {
            for (String match : names) {
                if (match.startsWith(buffer)) {
                    completeAdviser(candidates, match);
                }
            }
        }

        if (candidates.size() == 1) {
            completeEntity(candidates);
        }

        return candidates.isEmpty() ? -1 : 0;
    }

    private void completeEntity(List<CharSequence> candidates) {
        candidates.set(0, candidates.get(0) + " ");
    }

    private void completeAdviser(List<CharSequence> candidates, String match) {
        candidates.add(match);
    }

}
