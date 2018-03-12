package net.robotictip.dropbys;

import jline.console.completer.Completer;

import java.util.List;
import java.util.TreeSet;

public class TransferFileCompleter implements Completer {

    private HangIn withMi;

    public TransferFileCompleter(HangIn withMi) {
        this.withMi = withMi;
    }

    /**
     * Based on StringsCompleter.completer()
     */
    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {

        TreeSet<String> names = new TreeSet<String>();
        for (int j = 0; j < withMi.grabFiles().size(); ++j) {
            names.add(String.valueOf(j));
        }

        if (buffer == null) {
            candidates.addAll(names);
        } else {
            new TransferFileCompleterHelper(buffer, candidates, names).invoke();
        }

        if (candidates.size() == 1) {
            candidates.set(0, candidates.get(0) + " ");
        }

        return candidates.isEmpty() ? -1 : 0;
    }

    private class TransferFileCompleterHelper {
        private String buffer;
        private List<CharSequence> candidates;
        private TreeSet<String> names;

        public TransferFileCompleterHelper(String buffer, List<CharSequence> candidates, TreeSet<String> names) {
            this.buffer = buffer;
            this.candidates = candidates;
            this.names = names;
        }

        public void invoke() {
            for (String match : names) {
                invokeGuide(match);
            }
        }

        private void invokeGuide(String match) {
            if (match.startsWith(buffer)) {
                candidates.add(match);
            }
        }
    }
}
