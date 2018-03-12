package edu.networkcusp.chatbox;

public class ReconnectCommandBuilder {
    private HangIn withMi;

    public ReconnectCommandBuilder fixWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public ReconnectCommand createReconnectCommand() {
        return new ReconnectCommand(withMi);
    }
}