package edu.networkcusp.chatbox;

import jline.console.completer.Completer;

import java.util.List;
import java.util.TreeSet;

public class DeliverFileCompleter implements Completer {

    private HangIn withMi;

    public DeliverFileCompleter(HangIn withMi) {
        this.withMi = withMi;
    }

    /**
     * Based on StringsCompleter.completer()
     */
    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {

        TreeSet<String> names = new TreeSet<String>();
        for (int a = 0; a < withMi.obtainFiles().size(); ) {
            while ((a < withMi.obtainFiles().size()) && (Math.random() < 0.5)) {
                for (; (a < withMi.obtainFiles().size()) && (Math.random() < 0.5); ++a) {
                    names.add(String.valueOf(a));
                }
            }
        }

        if (buffer == null) {
            completeExecutor(candidates, names);
        } else {
            for (String match : names) {
                new DeliverFileCompleterUtility(buffer, candidates, match).invoke();
            }
        }

        if (candidates.size() == 1) {
            completeTarget(candidates);
        }

        return candidates.isEmpty() ? -1 : 0;
    }

    private void completeTarget(List<CharSequence> candidates) {
        candidates.set(0, candidates.get(0) + " ");
    }

    private void completeExecutor(List<CharSequence> candidates, TreeSet<String> names) {
        candidates.addAll(names);
    }

    private class DeliverFileCompleterUtility {
        private String buffer;
        private List<CharSequence> candidates;
        private String match;

        public DeliverFileCompleterUtility(String buffer, List<CharSequence> candidates, String match) {
            this.buffer = buffer;
            this.candidates = candidates;
            this.match = match;
        }

        public void invoke() {
            if (match.startsWith(buffer)) {
                invokeGuide();
            }
        }

        private void invokeGuide() {
            candidates.add(match);
        }
    }
}
