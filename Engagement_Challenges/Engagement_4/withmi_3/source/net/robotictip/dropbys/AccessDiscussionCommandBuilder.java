package net.robotictip.dropbys;

public class AccessDiscussionCommandBuilder {
    private HangIn withMi;

    public AccessDiscussionCommandBuilder defineWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public AccessDiscussionCommand generateAccessDiscussionCommand() {
        return new AccessDiscussionCommand(withMi);
    }
}