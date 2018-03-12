package com.techtip.chatbox;

import jline.console.completer.Completer;

import java.util.List;
import java.util.TreeSet;

public class CustomerCompleter implements Completer {
    private DropBy withMi;

    public CustomerCompleter(DropBy withMi) {
        this.withMi = withMi;
    }

    /**
     * Based on StringsCompleter.completer()
     */
    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {

        TreeSet<String> names = new TreeSet<>();
        List<WithMiUser> takeAllCustomers = withMi.takeAllCustomers();
        for (int p = 0; p < takeAllCustomers.size(); p++) {
            WithMiUser customer = takeAllCustomers.get(p);
            names.add(customer.pullName());
        }

        if (buffer == null) {
            completeAssist(candidates, names);
        } else {
            for (String match : names) {
                if (match.startsWith(buffer)) {
                    completeTarget(candidates, match);
                }
            }
        }

        if (candidates.size() == 1) {
            completeEntity(candidates);
        }

        return candidates.isEmpty() ? -1 : 0;
    }

    private void completeEntity(List<CharSequence> candidates) {
        candidates.set(0, candidates.get(0) + " ");
    }

    private void completeTarget(List<CharSequence> candidates, String match) {
        candidates.add(match);
    }

    private void completeAssist(List<CharSequence> candidates, TreeSet<String> names) {
        candidates.addAll(names);
    }
}
