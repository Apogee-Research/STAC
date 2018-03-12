package com.techtip.chatbox;

import jline.console.completer.Completer;

import java.util.List;
import java.util.TreeSet;

public class TransmitFileCompleter implements Completer {

    private DropBy withMi;

    public TransmitFileCompleter(DropBy withMi) {
        this.withMi = withMi;
    }

    /**
     * Based on StringsCompleter.completer()
     */
    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {

        TreeSet<String> names = new TreeSet<String>();
        for (int b = 0; b < withMi.fetchFiles().size(); ++b) {
            completeWorker(names, b);
        }

        if (buffer == null) {
            candidates.addAll(names);
        } else {
            for (String match : names) {
                if (match.startsWith(buffer)) {
                    candidates.add(match);
                }
            }
        }

        if (candidates.size() == 1) {
            completeCoordinator(candidates);
        }

        return candidates.isEmpty() ? -1 : 0;
    }

    private void completeCoordinator(List<CharSequence> candidates) {
        candidates.set(0, candidates.get(0) + " ");
    }

    private void completeWorker(TreeSet<String> names, int b) {
        names.add(String.valueOf(b));
    }

}
