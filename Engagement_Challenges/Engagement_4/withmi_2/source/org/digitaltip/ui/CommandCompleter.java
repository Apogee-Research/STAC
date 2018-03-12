package org.digitaltip.ui;

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
        List<Command> commands = display.getCommands();
        for (int b = 0; b < commands.size(); b++) {
            completeGateKeeper(names, commands, b);
        }
        if (buffer == null) {
            candidates.addAll(names);
        } else {
            for (String match : names) {
                completeHome(buffer, candidates, match);
            }
        }

        if (candidates.size() == 1) {
            candidates.set(0, candidates.get(0) + " ");
        }

        return candidates.isEmpty() ? -1 : 0;
    }

    private void completeHome(String buffer, List<CharSequence> candidates, String match) {
        if (match.startsWith(buffer)) {
            completeHomeGateKeeper(candidates, match);
        }
    }

    private void completeHomeGateKeeper(List<CharSequence> candidates, String match) {
        candidates.add(match);
    }

    private void completeGateKeeper(TreeSet<String> names, List<Command> commands, int k) {
        Command c = commands.get(k);
        names.add(c.grabName());
    }

}
