package net.computerpoint.chatroom;

import jline.console.completer.Completer;

import java.util.List;
import java.util.TreeSet;

public class EnterDiscussionCompleter implements Completer {

    private HangIn withMi;

    public EnterDiscussionCompleter(HangIn withMi) {
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
            completeGuide(candidates, names);
        } else {
            new EnterDiscussionCompleterExecutor(buffer, candidates, names).invoke();
        }

        if (candidates.size() == 1) {
            new EnterDiscussionCompleterManager(candidates).invoke();
        }
        return candidates.isEmpty() ? -1 : 0;
    }

    private void completeGuide(List<CharSequence> candidates, TreeSet<String> names) {
        candidates.addAll(names);
    }

    private class EnterDiscussionCompleterExecutor {
        private String buffer;
        private List<CharSequence> candidates;
        private TreeSet<String> names;

        public EnterDiscussionCompleterExecutor(String buffer, List<CharSequence> candidates, TreeSet<String> names) {
            this.buffer = buffer;
            this.candidates = candidates;
            this.names = names;
        }

        public void invoke() {
            for (String match : names) {
                invokeAid(match);
            }
        }

        private void invokeAid(String match) {
            if (match.startsWith(buffer)) {
                invokeAidAid(match);
            }
        }

        private void invokeAidAid(String match) {
            candidates.add(match);
        }
    }

    private class EnterDiscussionCompleterManager {
        private List<CharSequence> candidates;

        public EnterDiscussionCompleterManager(List<CharSequence> candidates) {
            this.candidates = candidates;
        }

        public void invoke() {
            candidates.set(0, candidates.get(0) + " ");
        }
    }
}
