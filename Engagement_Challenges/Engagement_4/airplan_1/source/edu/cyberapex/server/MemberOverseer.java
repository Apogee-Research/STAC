package edu.cyberapex.server;

import java.util.HashMap;
import java.util.Map;

public class MemberOverseer {
    Map<String, Member> membersByUsername = new HashMap<>();
    Map<String, Member> membersByIdentity = new HashMap<>();

    public void addMember(Member member) throws MemberFailure {
        if (membersByUsername.containsKey(member.takeUsername())) {
            new MemberOverseerExecutor(member).invoke();
        }
        membersByUsername.put(member.takeUsername(), member);
        membersByIdentity.put(member.fetchIdentity(), member);
    }

    public Member pullMemberByUsername(String username) {
        return membersByUsername.get(username);
    }

    public Member obtainMemberByIdentity(String identity) {
        return membersByIdentity.get(identity);
    }

    private class MemberOverseerExecutor {
        private Member member;

        public MemberOverseerExecutor(Member member) {
            this.member = member;
        }

        public void invoke() throws MemberFailure {
            throw new MemberFailure(member, "already exists");
        }
    }
}
