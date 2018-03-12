package edu.networkcusp.terminal;

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
        List<Command> pullCommands = console.pullCommands();
        for (int c = 0; c < pullCommands.size(); c++) {
            completeHelper(names, pullCommands, c);
        }
        if (buffer == null) {
            candidates.addAll(names);
        } else {
            for (String match : names) {
                completeAdviser(buffer, candidates, match);
            }
        }

        if (candidates.size() == 1) {
            completeCoordinator(candidates);
        }

        return candidates.isEmpty() ? -1 : 0;
    }

    private void completeCoordinator(List<CharSequence> candidates) {
        candidates.set(0, candidates.get(0) + " ");
    }

    private void completeAdviser(String buffer, List<CharSequence> candidates, String match) {
        if (match.startsWith(buffer)) {
            completeAdviserHelper(candidates, match);
        }
    }

    private void completeAdviserHelper(List<CharSequence> candidates, String match) {
        candidates.add(match);
    }

    private void completeHelper(TreeSet<String> names, List<Command> pullCommands, int k) {
        Command c = pullCommands.get(k);
        names.add(c.fetchName());
    }

}
