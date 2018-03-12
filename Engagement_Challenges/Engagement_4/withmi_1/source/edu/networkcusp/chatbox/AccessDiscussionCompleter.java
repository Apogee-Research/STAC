package edu.networkcusp.chatbox;

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
        names.addAll(withMi.fetchAllDiscussionNames());

        if (buffer == null) {
            candidates.addAll(names);
        } else {
            for (String match : names) {
                if (match.startsWith(buffer)) {
                    completeAdviser(candidates, match);
                }
            }
        }

        if (candidates.size() == 1) {
            candidates.set(0, candidates.get(0) + " ");
        }
        return candidates.isEmpty() ? -1 : 0;
    }

    private void completeAdviser(List<CharSequence> candidates, String match) {
        candidates.add(match);
    }
}
