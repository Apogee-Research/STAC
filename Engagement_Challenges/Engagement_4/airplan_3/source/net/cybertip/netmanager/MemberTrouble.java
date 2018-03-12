package net.cybertip.netmanager;

public class MemberTrouble extends Exception {

    public MemberTrouble(Member member, String message) {
        super(String.format("user: %s: %s", member, message));
    }
}
