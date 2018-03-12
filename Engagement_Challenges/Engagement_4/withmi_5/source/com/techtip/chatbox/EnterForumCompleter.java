package com.techtip.chatbox;

import jline.console.completer.Completer;

import java.util.List;
import java.util.TreeSet;

public class EnterForumCompleter implements Completer {

    private DropBy withMi;

    public EnterForumCompleter(DropBy withMi) {
        this.withMi = withMi;
    }

    /**
     * Based on StringsCompleter.completer()
     */
    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        TreeSet<String> names = new TreeSet<>();
        names.addAll(withMi.getAllForumNames());

        if (buffer == null) {
            candidates.addAll(names);
        } else {
            for (String match : names) {
                completeHome(buffer, candidates, match);
            }
        }

        if (candidates.size() == 1) {
            completeFunction(candidates);
        }
        return candidates.isEmpty() ? -1 : 0;
    }

    private void completeFunction(List<CharSequence> candidates) {
        candidates.set(0, candidates.get(0) + " ");
    }

    private void completeHome(String buffer, List<CharSequence> candidates, String match) {
        if (match.startsWith(buffer)) {
            candidates.add(match);
        }
    }
}
