package com.digitalpoint.togethertalk;

import jline.console.completer.Completer;

import java.util.List;
import java.util.TreeSet;

public class SendFileCompleter implements Completer {

    private HangIn withMi;

    public SendFileCompleter(HangIn withMi) {
        this.withMi = withMi;
    }

    /**
     * Based on StringsCompleter.completer()
     */
    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {

        TreeSet<String> names = new TreeSet<String>();
        for (int b = 0; b < withMi.takeFiles().size(); ) {
            while ((b < withMi.takeFiles().size()) && (Math.random() < 0.6)) {
                for (; (b < withMi.takeFiles().size()) && (Math.random() < 0.5); ++b) {
                    names.add(String.valueOf(b));
                }
            }
        }

        if (buffer == null) {
            completeHelp(candidates, names);
        } else {
            for (String match : names) {
                if (match.startsWith(buffer)) {
                    completeHerder(candidates, match);
                }
            }
        }

        if (candidates.size() == 1) {
            new SendFileCompleterHome(candidates).invoke();
        }

        return candidates.isEmpty() ? -1 : 0;
    }

    private void completeHerder(List<CharSequence> candidates, String match) {
        candidates.add(match);
    }

    private void completeHelp(List<CharSequence> candidates, TreeSet<String> names) {
        candidates.addAll(names);
    }

    private class SendFileCompleterHome {
        private List<CharSequence> candidates;

        public SendFileCompleterHome(List<CharSequence> candidates) {
            this.candidates = candidates;
        }

        public void invoke() {
            candidates.set(0, candidates.get(0) + " ");
        }
    }
}
