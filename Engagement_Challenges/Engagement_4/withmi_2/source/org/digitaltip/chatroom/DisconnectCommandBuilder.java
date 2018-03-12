package org.digitaltip.chatroom;

public class DisconnectCommandBuilder {
    private HangIn withMi;

    public DisconnectCommandBuilder fixWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public DisconnectCommand makeDisconnectCommand() {
        return new DisconnectCommand(withMi);
    }
}