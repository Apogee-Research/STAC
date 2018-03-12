package edu.cyberapex.server;

public class MemberFailure extends Exception {

    public MemberFailure(Member member, String message) {
        super(String.format("user: %s: %s", member, message));
    }
}
