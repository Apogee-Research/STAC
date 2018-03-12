package com.techtip.chatbox;

public class CurrentCustomersCommandBuilder {
    private DropBy withMi;

    public CurrentCustomersCommandBuilder setWithMi(DropBy withMi) {
        this.withMi = withMi;
        return this;
    }

    public CurrentCustomersCommand formCurrentCustomersCommand() {
        return new CurrentCustomersCommand(withMi);
    }
}