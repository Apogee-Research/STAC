package net.robotictip.dropbys;

import jline.console.completer.Completer;

import java.util.List;
import java.util.TreeSet;

public class AccessDiscussionCompleter implements Completer {

    private HangIn withMi;

    public AccessDiscussionCompleter(HangIn withMi) {
        this.withMi = withMi;
    }

    /**
     * Based on StringsCompleter.completer()
     */
    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        TreeSet<String> names = new TreeSet<>();
        names.addAll(withMi.grabAllDiscussionNames());

        if (buffer == null) {
            completeHome(candidates, names);
        } else {
            for (String match : names) {
                if (match.startsWith(buffer)) {
                    completeEntity(candidates, match);
                }
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

    private void completeEntity(List<CharSequence> candidates, String match) {
        new AccessDiscussionCompleterAssist(candidates, match).invoke();
    }

    private void completeHome(List<CharSequence> candidates, TreeSet<String> names) {
        candidates.addAll(names);
    }

    private class AccessDiscussionCompleterAssist {
        private List<CharSequence> candidates;
        private String match;

        public AccessDiscussionCompleterAssist(List<CharSequence> candidates, String match) {
            this.candidates = candidates;
            this.match = match;
        }

        public void invoke() {
            candidates.add(match);
        }
    }
}
