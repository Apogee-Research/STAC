package edu.networkcusp.chatbox;

public class AccessDiscussionCommandBuilder {
    private HangIn withMi;

    public AccessDiscussionCommandBuilder fixWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public AccessDiscussionCommand createAccessDiscussionCommand() {
        return new AccessDiscussionCommand(withMi);
    }
}