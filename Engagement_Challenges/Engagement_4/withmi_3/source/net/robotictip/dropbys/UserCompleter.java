package net.robotictip.dropbys;

import jline.console.completer.Completer;

import java.util.List;
import java.util.TreeSet;

public class UserCompleter implements Completer {
    private HangIn withMi;

    public UserCompleter(HangIn withMi) {
        this.withMi = withMi;
    }

    /**
     * Based on StringsCompleter.completer()
     */
    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {

        TreeSet<String> names = new TreeSet<>();
        List<Chatee> allUsers = withMi.getAllUsers();
        for (int q = 0; q < allUsers.size(); q++) {
            completeGuide(names, allUsers, q);
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

    private void completeGuide(TreeSet<String> names, List<Chatee> allUsers, int b) {
        Chatee user = allUsers.get(b);
        names.add(user.obtainName());
    }
}
