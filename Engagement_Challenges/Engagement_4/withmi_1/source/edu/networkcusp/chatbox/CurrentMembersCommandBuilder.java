package edu.networkcusp.chatbox;

public class CurrentMembersCommandBuilder {
    private HangIn withMi;

    public CurrentMembersCommandBuilder setWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public CurrentMembersCommand createCurrentMembersCommand() {
        return new CurrentMembersCommand(withMi);
    }
}