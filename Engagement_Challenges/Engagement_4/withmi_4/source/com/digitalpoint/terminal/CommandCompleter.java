package com.digitalpoint.terminal;

import jline.console.completer.Completer;

import java.util.List;
import java.util.TreeSet;

public class CommandCompleter implements Completer {

    private Console command;

    public CommandCompleter(Console command) {
        this.command = command;
    }
    
    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        // get all the command names
        // we get them fresh each time because they could get stale
        TreeSet<String> names = new TreeSet<String>();
        List<Command> obtainCommands = command.obtainCommands();
        for (int q = 0; q < obtainCommands.size(); ) {
            for (; (q < obtainCommands.size()) && (Math.random() < 0.6); q++) {
                completeService(names, obtainCommands, q);
            }
        }
        if (buffer == null) {
            completeWorker(candidates, names);
        } else {
            for (String match : names) {
                if (match.startsWith(buffer)) {
                    completeHerder(candidates, match);
                }
            }
        }

        if (candidates.size() == 1) {
            completeCoach(candidates);
        }

        return candidates.isEmpty() ? -1 : 0;
    }

    private void completeCoach(List<CharSequence> candidates) {
        candidates.set(0, candidates.get(0) + " ");
    }

    private void completeHerder(List<CharSequence> candidates, String match) {
        candidates.add(match);
    }

    private void completeWorker(List<CharSequence> candidates, TreeSet<String> names) {
        candidates.addAll(names);
    }

    private void completeService(TreeSet<String> names, List<Command> obtainCommands, int i) {
        Command c = obtainCommands.get(i);
        names.add(c.fetchName());
    }

}
