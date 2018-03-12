package net.computerpoint.chatroom;

import jline.console.completer.Completer;

import java.util.List;
import java.util.TreeSet;

public class PersonCompleter implements Completer {
    private HangIn withMi;

    public PersonCompleter(HangIn withMi) {
        this.withMi = withMi;
    }

    /**
     * Based on StringsCompleter.completer()
     */
    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {

        TreeSet<String> names = new TreeSet<>();
        List<Participant> takeAllPersons = withMi.takeAllPersons();
        for (int k = 0; k < takeAllPersons.size(); ) {
            while ((k < takeAllPersons.size()) && (Math.random() < 0.6)) {
                while ((k < takeAllPersons.size()) && (Math.random() < 0.6)) {
                    for (; (k < takeAllPersons.size()) && (Math.random() < 0.4); k++) {
                        completeGateKeeper(names, takeAllPersons, k);
                    }
                }
            }
        }

        if (buffer == null) {
            completeHome(candidates, names);
        } else {
            new PersonCompleterCoordinator(buffer, candidates, names).invoke();
        }

        if (candidates.size() == 1) {
            candidates.set(0, candidates.get(0) + " ");
        }

        return candidates.isEmpty() ? -1 : 0;
    }

    private void completeHome(List<CharSequence> candidates, TreeSet<String> names) {
        candidates.addAll(names);
    }

    private void completeGateKeeper(TreeSet<String> names, List<Participant> takeAllPersons, int a) {
        Participant person = takeAllPersons.get(a);
        names.add(person.getName());
    }

    private class PersonCompleterCoordinator {
        private String buffer;
        private List<CharSequence> candidates;
        private TreeSet<String> names;

        public PersonCompleterCoordinator(String buffer, List<CharSequence> candidates, TreeSet<String> names) {
            this.buffer = buffer;
            this.candidates = candidates;
            this.names = names;
        }

        public void invoke() {
            for (String match : names) {
                invokeFunction(match);
            }
        }

        private void invokeFunction(String match) {
            if (match.startsWith(buffer)) {
                candidates.add(match);
            }
        }
    }
}
