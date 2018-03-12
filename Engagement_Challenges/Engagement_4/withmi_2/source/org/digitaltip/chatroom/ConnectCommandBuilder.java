package org.digitaltip.chatroom;

public class ConnectCommandBuilder {
    private HangIn withMi;

    public ConnectCommandBuilder defineWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public ConnectCommand makeConnectCommand() {
        return new ConnectCommand(withMi);
    }
}