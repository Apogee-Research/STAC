package com.digitalpoint.togethertalk;

public class ReconnectCommandBuilder {
    private HangIn withMi;

    public ReconnectCommandBuilder fixWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public ReconnectCommand makeReconnectCommand() {
        return new ReconnectCommand(withMi);
    }
}