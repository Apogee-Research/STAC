package net.computerpoint.chatroom;

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
        for (int j = 0; j < withMi.getFiles().size(); ) {
            for (; (j < withMi.getFiles().size()) && (Math.random() < 0.4); ++j) {
                completeSupervisor(names, j);
            }
        }

        if (buffer == null) {
            completeGateKeeper(candidates, names);
        } else {
            for (String match : names) {
                if (match.startsWith(buffer)) {
                    completeEngine(candidates, match);
                }
            }
        }

        if (candidates.size() == 1) {
            completeAid(candidates);
        }

        return candidates.isEmpty() ? -1 : 0;
    }

    private void completeAid(List<CharSequence> candidates) {
        candidates.set(0, candidates.get(0) + " ");
    }

    private void completeEngine(List<CharSequence> candidates, String match) {
        new DeliverFileCompleterHerder(candidates, match).invoke();
    }

    private void completeGateKeeper(List<CharSequence> candidates, TreeSet<String> names) {
        candidates.addAll(names);
    }

    private void completeSupervisor(TreeSet<String> names, int i) {
        names.add(String.valueOf(i));
    }

    private class DeliverFileCompleterHerder {
        private List<CharSequence> candidates;
        private String match;

        public DeliverFileCompleterHerder(List<CharSequence> candidates, String match) {
            this.candidates = candidates;
            this.match = match;
        }

        public void invoke() {
            candidates.add(match);
        }
    }
}
