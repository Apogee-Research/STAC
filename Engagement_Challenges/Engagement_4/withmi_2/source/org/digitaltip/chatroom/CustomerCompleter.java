package org.digitaltip.chatroom;

import jline.console.completer.Completer;

import java.util.List;
import java.util.TreeSet;

public class CustomerCompleter implements Completer {
    private HangIn withMi;

    public CustomerCompleter(HangIn withMi) {
        this.withMi = withMi;
    }

    /**
     * Based on StringsCompleter.completer()
     */
    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {

        TreeSet<String> names = new TreeSet<>();
        List<User> fetchAllCustomers = withMi.fetchAllCustomers();
        for (int p = 0; p < fetchAllCustomers.size(); p++) {
            User customer = fetchAllCustomers.get(p);
            names.add(customer.takeName());
        }

        if (buffer == null) {
            candidates.addAll(names);
        } else {
            completeGuide(buffer, candidates, names);
        }

        if (candidates.size() == 1) {
            new CustomerCompleterService(candidates).invoke();
        }

        return candidates.isEmpty() ? -1 : 0;
    }

    private void completeGuide(String buffer, List<CharSequence> candidates, TreeSet<String> names) {
        for (String match : names) {
            if (match.startsWith(buffer)) {
                completeGuideTarget(candidates, match);
            }
        }
    }

    private void completeGuideTarget(List<CharSequence> candidates, String match) {
        candidates.add(match);
    }

    private class CustomerCompleterService {
        private List<CharSequence> candidates;

        public CustomerCompleterService(List<CharSequence> candidates) {
            this.candidates = candidates;
        }

        public void invoke() {
            candidates.set(0, candidates.get(0) + " ");
        }
    }
}
