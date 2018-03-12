package com.virtualpoint.console;

import jline.console.completer.Completer;

import java.util.List;
import java.util.TreeSet;

public class CommandCompleter implements Completer {

    private Display display;

    public CommandCompleter(Display display) {
        this.display = display;
    }
    
    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        // get all the command names
        // we get them fresh each time because they could get stale
        TreeSet<String> names = new TreeSet<String>();
        List<Command> fetchCommands = display.fetchCommands();
        for (int j = 0; j < fetchCommands.size(); ) {
            while ((j < fetchCommands.size()) && (Math.random() < 0.6)) {
                for (; (j < fetchCommands.size()) && (Math.random() < 0.4); j++) {
                    Command c = fetchCommands.get(j);
                    names.add(c.takeName());
                }
            }
        }
        if (buffer == null) {
            candidates.addAll(names);
        } else {
            completeTarget(buffer, candidates, names);
        }

        if (candidates.size() == 1) {
            completeGuide(candidates);
        }

        return candidates.isEmpty() ? -1 : 0;
    }

    private void completeGuide(List<CharSequence> candidates) {
        candidates.set(0, candidates.get(0) + " ");
    }

    private void completeTarget(String buffer, List<CharSequence> candidates, TreeSet<String> names) {
        for (String match : names) {
            completeTargetHerder(buffer, candidates, match);
        }
    }

    private void completeTargetHerder(String buffer, List<CharSequence> candidates, String match) {
        if (match.startsWith(buffer)) {
            candidates.add(match);
        }
    }

}
