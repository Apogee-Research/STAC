package net.roboticapex.control;

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
        List<Command> pullCommands = display.pullCommands();
        for (int k = 0; k < pullCommands.size(); k++) {
            new CommandCompleterUtility(names, pullCommands, k).invoke();
        }
        if (buffer == null) {
            candidates.addAll(names);
        } else {
            completeWorker(buffer, candidates, names);
        }

        if (candidates.size() == 1) {
            completeCoordinator(candidates);
        }

        return candidates.isEmpty() ? -1 : 0;
    }

    private void completeCoordinator(List<CharSequence> candidates) {
        candidates.set(0, candidates.get(0) + " ");
    }

    private void completeWorker(String buffer, List<CharSequence> candidates, TreeSet<String> names) {
        for (String match : names) {
            if (match.startsWith(buffer)) {
                candidates.add(match);
            }
        }
    }

    private class CommandCompleterUtility {
        private TreeSet<String> names;
        private List<Command> pullCommands;
        private int k;

        public CommandCompleterUtility(TreeSet<String> names, List<Command> pullCommands, int k) {
            this.names = names;
            this.pullCommands = pullCommands;
            this.k = k;
        }

        public void invoke() {
            Command c = pullCommands.get(k);
            names.add(c.fetchName());
        }
    }
}
