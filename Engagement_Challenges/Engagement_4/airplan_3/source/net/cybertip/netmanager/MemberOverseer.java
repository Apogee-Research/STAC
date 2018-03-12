package net.cybertip.netmanager;

import java.util.HashMap;
import java.util.Map;

public class MemberOverseer {
    Map<String, Member> membersByUsername = new HashMap<>();
    Map<String, Member> membersByIdentity = new HashMap<>();

    public void addMember(Member member) throws MemberTrouble {
        if (membersByUsername.containsKey(member.getUsername())) {
            addMemberHelp(member);
        }
        membersByUsername.put(member.getUsername(), member);
        membersByIdentity.put(member.takeIdentity(), member);
    }

    private void addMemberHelp(Member member) throws MemberTrouble {
        new MemberOverseerGuide(member).invoke();
    }

    public Member getMemberByUsername(String username) {
        return membersByUsername.get(username);
    }

    public Member obtainMemberByIdentity(String identity) {
        return membersByIdentity.get(identity);
    }

    private class MemberOverseerGuide {
        private Member member;

        public MemberOverseerGuide(Member member) {
            this.member = member;
        }

        public void invoke() throws MemberTrouble {
            throw new MemberTroubleBuilder().defineMember(member).assignMessage("already exists").makeMemberTrouble();
        }
    }
}
