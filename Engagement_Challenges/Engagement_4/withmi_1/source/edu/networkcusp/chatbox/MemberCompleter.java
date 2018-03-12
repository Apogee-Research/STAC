package edu.networkcusp.chatbox;

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
        List<WithMiUser> allMembers = withMi.getAllMembers();
        for (int j = 0; j < allMembers.size(); j++) {
            completeHelp(names, allMembers, j);
        }

        if (buffer == null) {
            completeSupervisor(candidates, names);
        } else {
            for (String match : names) {
                completeCoordinator(buffer, candidates, match);
            }
        }

        if (candidates.size() == 1) {
            candidates.set(0, candidates.get(0) + " ");
        }

        return candidates.isEmpty() ? -1 : 0;
    }

    private void completeCoordinator(String buffer, List<CharSequence> candidates, String match) {
        if (match.startsWith(buffer)) {
            candidates.add(match);
        }
    }

    private void completeSupervisor(List<CharSequence> candidates, TreeSet<String> names) {
        candidates.addAll(names);
    }

    private void completeHelp(TreeSet<String> names, List<WithMiUser> allMembers, int a) {
        WithMiUser member = allMembers.get(a);
        names.add(member.obtainName());
    }
}
