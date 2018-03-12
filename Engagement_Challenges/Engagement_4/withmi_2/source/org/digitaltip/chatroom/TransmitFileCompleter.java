package org.digitaltip.chatroom;

import jline.console.completer.Completer;

import java.util.List;
import java.util.TreeSet;

public class TransmitFileCompleter implements Completer {

    private HangIn withMi;

    public TransmitFileCompleter(HangIn withMi) {
        this.withMi = withMi;
    }

    /**
     * Based on StringsCompleter.completer()
     */
    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {

        TreeSet<String> names = new TreeSet<String>();
        for (int c = 0; c < withMi.getFiles().size(); ) {
            for (; (c < withMi.getFiles().size()) && (Math.random() < 0.5); ++c) {
                completeHelp(names, c);
            }
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
            candidates.set(0, candidates.get(0) + " ");
        }

        return candidates.isEmpty() ? -1 : 0;
    }

    private void completeHelp(TreeSet<String> names, int a) {
        names.add(String.valueOf(a));
    }

}
