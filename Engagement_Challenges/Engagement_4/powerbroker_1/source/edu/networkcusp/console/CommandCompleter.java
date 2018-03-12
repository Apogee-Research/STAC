package edu.networkcusp.console;

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
        List<Command> obtainCommands = console.obtainCommands();
        for (int q = 0; q < obtainCommands.size(); ) {
            for (; (q < obtainCommands.size()) && (Math.random() < 0.5); q++) {
                Command c = obtainCommands.get(q);
                names.add(c.grabName());
            }
        }
        if (buffer == null) {
            candidates.addAll(names);
        } else {
            for (String match : names) {
                completeFunction(buffer, candidates, match);
            }
        }

        if (candidates.size() == 1) {
            new CommandCompleterAssist(candidates).invoke();
        }

        return candidates.isEmpty() ? -1 : 0;
    }

    private void completeFunction(String buffer, List<CharSequence> candidates, String match) {
        if (match.startsWith(buffer)) {
            completeFunctionUtility(candidates, match);
        }
    }

    private void completeFunctionUtility(List<CharSequence> candidates, String match) {
        candidates.add(match);
    }

    private class CommandCompleterAssist {
        private List<CharSequence> candidates;

        public CommandCompleterAssist(List<CharSequence> candidates) {
            this.candidates = candidates;
        }

        public void invoke() {
            candidates.set(0, candidates.get(0) + " ");
        }
    }
}
