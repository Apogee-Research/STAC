package org.digitaltip.chatroom;

public class MakeGroupConferenceCommandBuilder {
    private HangIn withMi;

    public MakeGroupConferenceCommandBuilder fixWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public MakeGroupConferenceCommand makeMakeGroupConferenceCommand() {
        return new MakeGroupConferenceCommand(withMi);
    }
}