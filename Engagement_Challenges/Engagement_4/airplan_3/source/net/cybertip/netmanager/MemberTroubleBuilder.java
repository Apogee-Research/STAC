package net.cybertip.netmanager;

public class MemberTroubleBuilder {
    private String message;
    private Member member;

    public MemberTroubleBuilder assignMessage(String message) {
        this.message = message;
        return this;
    }

    public MemberTroubleBuilder defineMember(Member member) {
        this.member = member;
        return this;
    }

    public MemberTrouble makeMemberTrouble() {
        return new MemberTrouble(member, message);
    }
}