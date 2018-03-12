package com.digitalpoint.togethertalk;

public class MemberCompleterBuilder {
    private HangIn withMi;

    public MemberCompleterBuilder assignWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public MemberCompleter makeMemberCompleter() {
        return new MemberCompleter(withMi);
    }
}