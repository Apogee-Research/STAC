package com.techtip.control;

import jline.console.completer.Completer;

import java.util.List;
import java.util.TreeSet;

public class CommandCompleter implements Completer {

    private Ui ui;

    public CommandCompleter(Ui ui) {
        this.ui = ui;
    }
    
    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        // get all the command names
        // we get them fresh each time because they could get stale
        TreeSet<String> names = new TreeSet<String>();
        List<Command> obtainCommands = ui.obtainCommands();
        for (int j = 0; j < obtainCommands.size(); j++) {
            completeEntity(names, obtainCommands, j);
        }
        if (buffer == null) {
            completeGuide(candidates, names);
        } else {
            completeHelper(buffer, candidates, names);
        }

        if (candidates.size() == 1) {
            completeCoordinator(candidates);
        }

        return candidates.isEmpty() ? -1 : 0;
    }

    private void completeCoordinator(List<CharSequence> candidates) {
        candidates.set(0, candidates.get(0) + " ");
    }

    private void completeHelper(String buffer, List<CharSequence> candidates, TreeSet<String> names) {
        for (String match : names) {
            if (match.startsWith(buffer)) {
                candidates.add(match);
            }
        }
    }

    private void completeGuide(List<CharSequence> candidates, TreeSet<String> names) {
        candidates.addAll(names);
    }

    private void completeEntity(TreeSet<String> names, List<Command> obtainCommands, int q) {
        Command c = obtainCommands.get(q);
        names.add(c.grabName());
    }

}
