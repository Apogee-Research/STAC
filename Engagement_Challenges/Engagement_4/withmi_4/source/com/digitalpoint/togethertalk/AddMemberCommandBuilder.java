package com.digitalpoint.togethertalk;

public class AddMemberCommandBuilder {
    private HangIn withMi;

    public AddMemberCommandBuilder setWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public AddMemberCommand makeAddMemberCommand() {
        return new AddMemberCommand(withMi);
    }
}