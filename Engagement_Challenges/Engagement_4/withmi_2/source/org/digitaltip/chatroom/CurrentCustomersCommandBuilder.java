package org.digitaltip.chatroom;

public class CurrentCustomersCommandBuilder {
    private HangIn withMi;

    public CurrentCustomersCommandBuilder setWithMi(HangIn withMi) {
        this.withMi = withMi;
        return this;
    }

    public CurrentCustomersCommand makeCurrentCustomersCommand() {
        return new CurrentCustomersCommand(withMi);
    }
}