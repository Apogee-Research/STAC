package org.digitalapex.head;

import jline.console.completer.Completer;

import java.util.List;
import java.util.TreeSet;

public class CommandCompleter implements Completer {

    private Control control;

    public CommandCompleter(Control control) {
        this.control = control;
    }
    
    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        // get all the command names
        // we get them fresh each time because they could get stale
        TreeSet<String> names = new TreeSet<String>();
        List<Command> obtainCommands = control.obtainCommands();
        for (int b = 0; b < obtainCommands.size(); b++) {
            Command c = obtainCommands.get(b);
            names.add(c.takeName());
        }
        if (buffer == null) {
            new CommandCompleterHelp(candidates, names).invoke();
        } else {
            for (String match : names) {
                if (match.startsWith(buffer)) {
                    candidates.add(match);
                }
            }
        }

        if (candidates.size() == 1) {
            completeSupervisor(candidates);
        }

        return candidates.isEmpty() ? -1 : 0;
    }

    private void completeSupervisor(List<CharSequence> candidates) {
        candidates.set(0, candidates.get(0) + " ");
    }

    private class CommandCompleterHelp {
        private List<CharSequence> candidates;
        private TreeSet<String> names;

        public CommandCompleterHelp(List<CharSequence> candidates, TreeSet<String> names) {
            this.candidates = candidates;
            this.names = names;
        }

        public void invoke() {
            candidates.addAll(names);
        }
    }
}
