package com.digitalpoint.togethertalk;

import jline.console.completer.Completer;

import java.util.List;
import java.util.TreeSet;

public class MemberCompleter implements Completer {
    private HangIn withMi;

    public MemberCompleter(HangIn withMi) {
        this.withMi = withMi;
    }

    /**
     * Based on StringsCompleter.completer()
     */
    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {

        TreeSet<String> names = new TreeSet<>();
        List<Participant> fetchAllMembers = withMi.fetchAllMembers();
        for (int b = 0; b < fetchAllMembers.size(); b++) {
            Participant member = fetchAllMembers.get(b);
            names.add(member.grabName());
        }

        if (buffer == null) {
            candidates.addAll(names);
        } else {
            new MemberCompleterHerder(buffer, candidates, names).invoke();
        }

        if (candidates.size() == 1) {
            completeEntity(candidates);
        }

        return candidates.isEmpty() ? -1 : 0;
    }

    private void completeEntity(List<CharSequence> candidates) {
        candidates.set(0, candidates.get(0) + " ");
    }

    private class MemberCompleterHerder {
        private String buffer;
        private List<CharSequence> candidates;
        private TreeSet<String> names;

        public MemberCompleterHerder(String buffer, List<CharSequence> candidates, TreeSet<String> names) {
            this.buffer = buffer;
            this.candidates = candidates;
            this.names = names;
        }

        public void invoke() {
            for (String match : names) {
                if (match.startsWith(buffer)) {
                    candidates.add(match);
                }
            }
        }
    }
}
