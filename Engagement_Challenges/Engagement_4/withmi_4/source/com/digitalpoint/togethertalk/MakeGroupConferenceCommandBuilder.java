package com.digitalpoint.togethertalk;

public class MakeGroupConferenceCommandBuilder {
    private HangIn withMi;

    public MakeGroupConferenceCommandBuilder defineWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public MakeGroupConferenceCommand makeMakeGroupConferenceCommand() {
        return new MakeGroupConferenceCommand(withMi);
    }
}