package com.cyberpointllc.stac.console;

import java.util.List;
import java.util.TreeSet;
import jline.console.completer.Completer;

public class CommandCompleter implements Completer {

    private Console console;

    public CommandCompleter(Console console) {
        this.console = console;
    }

    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        // get all the command names
        // we get them fresh each time because they could get stale
        TreeSet<String> names = new  TreeSet<String>();
        for (Command c : console.getCommands()) {
            completeHelper(c, names);
        }
        if (buffer == null) {
            completeHelper1(names, candidates);
        } else {
            completeHelper2(buffer, names, candidates);
        }
        if (candidates.size() == 1) {
            completeHelper3(candidates);
        }
        return candidates.isEmpty() ? -1 : 0;
    }

    private void completeHelper(Command c, TreeSet<String> names) {
        names.add(c.getName());
    }

    private void completeHelper1(TreeSet<String> names, List<CharSequence> candidates) {
        candidates.addAll(names);
    }

    private void completeHelper2(String buffer, TreeSet<String> names, List<CharSequence> candidates) {
        for (String match : names) {
            if (match.startsWith(buffer)) {
                candidates.add(match);
            }
        }
    }

    private void completeHelper3(List<CharSequence> candidates) {
        candidates.set(0, candidates.get(0) + " ");
    }
}
