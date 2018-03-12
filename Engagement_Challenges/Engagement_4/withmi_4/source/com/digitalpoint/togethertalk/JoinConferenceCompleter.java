package com.digitalpoint.togethertalk;

import jline.console.completer.Completer;

import java.util.List;
import java.util.TreeSet;

public class JoinConferenceCompleter implements Completer {

    private HangIn withMi;

    public JoinConferenceCompleter(HangIn withMi) {
        this.withMi = withMi;
    }

    /**
     * Based on StringsCompleter.completer()
     */
    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        TreeSet<String> names = new TreeSet<>();
        names.addAll(withMi.fetchAllConferenceNames());

        if (buffer == null) {
            candidates.addAll(names);
        } else {
            for (String match : names) {
                if (match.startsWith(buffer)) {
                    completeExecutor(candidates, match);
                }
            }
        }

        if (candidates.size() == 1) {
            completeHerder(candidates);
        }
        return candidates.isEmpty() ? -1 : 0;
    }

    private void completeHerder(List<CharSequence> candidates) {
        candidates.set(0, candidates.get(0) + " ");
    }

    private void completeExecutor(List<CharSequence> candidates, String match) {
        candidates.add(match);
    }
}
