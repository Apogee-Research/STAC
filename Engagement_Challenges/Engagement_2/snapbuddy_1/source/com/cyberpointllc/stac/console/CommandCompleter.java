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
            candidates.addAll(names);
        } else {
            for (String match : names) {
                if (match.startsWith(buffer)) {
                    candidates.add(match);
                }
            }
        }
        if (candidates.size() == 1) {
            candidates.set(0, candidates.get(0) + " ");
        }
        return candidates.isEmpty() ? -1 : 0;
    }

    private void completeHelper(Command c, TreeSet<String> names) {
        names.add(c.getName());
    }
}
