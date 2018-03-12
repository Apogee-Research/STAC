package com.digitalpoint.togethertalk;

public class CurrentConferenceCommandBuilder {
    private HangIn withMi;

    public CurrentConferenceCommandBuilder defineWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public CurrentConferenceCommand makeCurrentConferenceCommand() {
        return new CurrentConferenceCommand(withMi);
    }
}