package com.digitalpoint.togethertalk;

public class JoinConferenceCompleterBuilder {
    private HangIn withMi;

    public JoinConferenceCompleterBuilder setWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public JoinConferenceCompleter makeJoinConferenceCompleter() {
        return new JoinConferenceCompleter(withMi);
    }
}